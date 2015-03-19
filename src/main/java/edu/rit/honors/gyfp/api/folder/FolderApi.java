package edu.rit.honors.gyfp.api.folder;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import edu.rit.honors.gyfp.api.ApiUtil;
import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.api.model.FileUser;
import edu.rit.honors.gyfp.api.model.Folder;
import edu.rit.honors.gyfp.api.model.TransferRequest;
import edu.rit.honors.gyfp.api.model.TransferableFile;
import edu.rit.honors.gyfp.util.OfyService;
import edu.rit.honors.gyfp.util.Utils;

import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Api(
        name = "gyfp",
        version = "v1",
        scopes = {
                Constants.Scope.USER_EMAIL,
                Constants.Scope.DRIVE_FULL
        },
        clientIds = {
                Constants.Clients.WEB_CLIENT
        })
public class FolderApi {

    private static final Logger log = Logger.getLogger(FolderApi.class.getName());

    /**
     * Gets a folder object which contains ownership information for all files contained under the requested fileid.
     * This request is recursive and will take time for large requests.
     *
     * @param id
     *         The google fileid of the folder that will be inspected
     * @param user
     *         The user making the request. Required for authorization purposes.
     *
     * @return The folder, with all children loaded
     *
     * @throws NotFoundException
     *         If a folder with fileid id is not found
     * @throws ForbiddenException
     *         If the user is not the owner of the folder
     * @throws BadRequestException
     *         If the id is not a folder, or if the id is not provided
     */
    @ApiMethod(name = "folders.get", httpMethod = HttpMethod.GET)
    public Folder getFolder(@Named("id") String id, User user,
                            @Named("ignoreCache") @Nullable Boolean ignoreCache)
            throws NotFoundException, ForbiddenException, BadRequestException {

        if (Strings.isNullOrEmpty(id)) {
            throw new BadRequestException(String.format(Constants.Error.MISSING_PARAMETER, "id"));
        }

        if (user == null) {
            throw new ForbiddenException(Constants.Error.AUTH_REQUIRED);
        }

        Drive service = Utils.createDriveFromUser(user);

        File result;
        try {
            result = service.files().get(id).setFields(Constants.Field.KIND_OWNER).execute();
        } catch (IOException e) {
            log.severe(e.getMessage());
            log.log(Level.SEVERE, "Could not load file " + id, e);
            throw new NotFoundException(String.format(Constants.Error.LOAD_FOLDER_FOR_USER_FAIL, id, user.getNickname(), user.getEmail(), e.getMessage()), e);
        }

        if (!Constants.MimeType.FOLDER.equals(result.getMimeType())) {
            throw new BadRequestException(String.format(Constants.Error.INVALID_MIME, result.getMimeType()));
        }

        // At this point we know that:
        //   1)  There is a file with the given id
        //   2)  The "file" is actually a folder
        //
        // This means we can safely begin attempting to load the contents of the folder.

        // Default to not ignoring the cache
        ignoreCache = ignoreCache == null ? false : ignoreCache;
        return Folder.fromGoogleId(id, user, ignoreCache);
    }

    /**
     * Removes read permissions for all files in the given folder from the list of users specified.
     *
     * @param folder
     *         The id of the folder from which access will be revoked
     * @param user
     *         The user making the request. Required for authorization purposes
     * @param userId
     *         The ID of the user who will have read permissions removed
     *
     * @return the updated folder
     *
     * @throws NotFoundException
     *         If a folder with fileid id is not found
     * @throws ForbiddenException
     *         If the user is not the owner of the folder
     */
    @ApiMethod(name = "folders.revoke.reader", httpMethod = HttpMethod.POST)
    public Folder revokeReadPermission(@Named("folder") String folder, User user, @Named("userId") String userId)
            throws NotFoundException, ForbiddenException, BadRequestException, InternalServerErrorException {

        return revokePermission(folder, user, userId, Constants.Role.READER);
    }

    /**
     * Removes write permissions for all files in the given folder from the list of users specified.
     *
     * @param folder
     *         The id of the folder from which access will be revoked
     * @param user
     *         The user making the request. Required for authorization purposes
     * @param userId
     *         The ID of the user who will have read permissions removed
     *
     * @return the updated folder
     *
     * @throws NotFoundException
     *         If a folder with fileid id is not found
     * @throws ForbiddenException
     *         If the user is not the owner of the folder
     * @throws BadRequestException
     *         If there are no users specified or no permissions were revoked
     */
    @ApiMethod(name = "folders.revoke.writer", httpMethod = HttpMethod.POST)
    public Folder revokeWritePermission(@Named("folder") String folder, User user, @Named("userId") String userId)
            throws NotFoundException, ForbiddenException, BadRequestException, InternalServerErrorException {

        return revokePermission(folder, user, userId, Constants.Role.WRITER);
    }

    /**
     * @param folder
     *         The id of the folder from which access will be revoked
     * @param user
     *         The user making the request. Required for authorization purposes
     * @param userId
     *         The ID of the user who will have read permissions removed
     * @param role
     *         The role (read or write)
     *
     * @return the updated folder
     *
     * @throws ForbiddenException
     * @throws BadRequestException
     */
    private Folder revokePermission(String folder, User user, String userId, String role)
            throws NotFoundException, ForbiddenException, BadRequestException,
            InternalServerErrorException {
        if (user == null) {
            throw new ForbiddenException(Constants.Error.AUTH_REQUIRED);
        }

        if (userId == null || userId.length() == 0) {
            throw new BadRequestException(String.format(Constants.Error.MISSING_PARAMETER, "userId"));
        }

        Folder target = Folder.fromGoogleId(folder, user);

        Drive service = Utils.createDriveFromUser(user);
        FileUser targetUser = target.getUser(userId);

        if (targetUser == null) {
            throw new NotFoundException("User " + userId + " does not have any files in the specified folder");
        }


        BatchRequest batch = service.batch();
        final Set<TransferableFile> success = new HashSet<>();
        for (final TransferableFile f : targetUser.getFiles(role)) {

            // Limit the number of requests to 100 at a time to somewhat come closer to the rate limit...
            if (batch.size() >= 100) {
                break;
            }

            try {
                service.permissions().delete(f.getFileId(), targetUser.getPermission()).queue(batch, new JsonBatchCallback<Void>() {
                    @Override
                    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                        log.log(Level.WARNING, "Could not delete permission for file " + f.getFileId(), e);
                    }

                    @Override
                    public void onSuccess(Void aVoid, HttpHeaders responseHeaders) throws IOException {
                        success.add(f);
                    }
                });
            } catch (IOException e) {
                log.log(Level.WARNING, "Could not delete permission for file " + f.getFileId(), e);
            }
        }

        try {
            batch.execute();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error executing batch", e);
        }

        targetUser.getFiles(role).removeAll(success);


        OfyService.ofy().save().entity(target).now();
        return target;
    }

    /**
     * Creates "polite" transfer requests for all the specified users which will transfer any files owned in the given
     * folder to the requesting user
     *
     * @param folderid
     *         The id of folder for which the transfer requests will be created
     * @param user
     *         The user making the request. Required for authorization purposes
     * @param users
     *         A list of users who will have transfer requests made for their files
     *
     * @return A list of TransferRequests, one for each user who owned files in the folder.
     *
     * @throws NotFoundException
     *         If a folder with fileid id is not found
     * @throws ForbiddenException
     *         If the user is not the owner of the folder
     * @throws BadRequestException
     *         If no users are specified
     */
    @ApiMethod(name = "folders.transfer.polite", httpMethod = HttpMethod.POST)
    public List<TransferRequest> makeTransferRequest(@Named("folder") String folderid, User user, @Named("users") List<String> users)
            throws NotFoundException, ForbiddenException, BadRequestException {

        return getTransferRequests(folderid, user, users, false);
    }

    /**
     * Creates "hostile" transfer requests for all the specified users which will transfer any files owned in the given
     * folder to the requesting user.
     * <p/>
     * Unlike the "polite" transfer requests, this API call will actually forcefully transfer ownership of all files to
     * the requesting user
     *
     * @param folderid
     *         The id of the folder for which the transfer requests will be created
     * @param user
     *         The user making the request. Required for authorization purposes
     * @param users
     *         A list of users who will have transfer requests made for their files
     *
     * @return A list of TransferRequests, one for each user who owned files in the folder.
     *
     * @throws NotFoundException
     *         If a folder with fileid id is not found
     * @throws ForbiddenException
     *         If the user is not the owner of the folder
     * @throws BadRequestException
     *         If no users are specified
     */
    @ApiMethod(name = "folders.transfer.hostile", httpMethod = HttpMethod.POST)
    public List<TransferRequest> makeHostileTransferRequest(@Named("folder") String folderid, User user, @Named("users") List<String> users)
            throws NotFoundException, ForbiddenException, BadRequestException {

        return getTransferRequests(folderid, user, users, true);
    }

    private List<TransferRequest> getTransferRequests(String folderid, User user, List<String> users, boolean isForced) throws ForbiddenException, BadRequestException {
        if (user == null) {
            throw new ForbiddenException(Constants.Error.AUTH_REQUIRED);
        }

        if (users == null || users.isEmpty()) {
            throw new BadRequestException(String.format(Constants.Error.MISSING_PARAMETER, "users"));
        }

        Folder folder = Folder.fromGoogleId(folderid, user);
        List<TransferRequest> requests = new ArrayList<>();

        for (String userId : users) {
            TransferRequest request = TransferRequest.fromFolder(folder, user, userId);
            request.setIsForced(isForced);
            requests.add(request);

            sendTransferRequestMessage(request);
        }
        return requests;
    }

    private void sendTransferRequestMessage(TransferRequest request) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        // TODO make this message contain useful information
        String messageText = "Hey there! \n"
                + "    " + request.getRequestingUser().getEmail() + " has requested that you transfer ownership of "
                + "your files in the folder  <a href=\"https://gimmeyoufilesplease.appspot.com/#/request/" + request.getId() + "\">" + request.getId() + "</a>.";

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Constants.Email.ADDRESS, Constants.Email.NAME));
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(request.getTargetUser().getEmail(), request.getTargetUser().getName()));
            message.setSubject("You received a transfer request");
            message.setText(messageText);

            log.info("Sending transfer email from " + Constants.Email.ADDRESS + " (" + Constants.Email.NAME + ") to " + request.getTargetUser().getEmail() + " for request " + request.getId());
            Transport.send(message);

        } catch (AddressException e) {
            log.log(Level.WARNING, "Invalid email address specified", e);
        } catch (MessagingException e) {
            log.log(Level.SEVERE, "Error sending email", e);
        } catch (UnsupportedEncodingException e) {
            log.log(Level.INFO, "Unsupported Encoding", e);
        }
    }

    /**
     * Converts existing polite transfer requests into hostile requests and executes them.
     *
     * @param requests
     *         A list of all the ids of the transfer requests that will be force-completed
     * @param user
     *         The user making the request. Required for authorization purposes
     *
     * @return A list of the modified TransferRequests
     *
     * @throws ForbiddenException
     *         If the user is not the owner of the folder
     */
    @ApiMethod(name = "folders.transfer.convert", httpMethod = HttpMethod.PUT)
    public List<TransferRequest> convertTransferRequest(@Named("requests") List<Long> requests, User user)
            throws ForbiddenException {
        // TODO
        return new ArrayList<>();
    }


    /**
     * Gets a list of the pending transfer requests for a given folder
     *
     * @param folder
     *         The id of the folder for which transfer requests will be listed
     * @param user
     *         The user making the request. Required for authorization purposes.
     *
     * @return The folder, with all children loaded
     *
     * @throws NotFoundException
     *         If a folder with the fiven id is not found
     * @throws ForbiddenException
     *         If the user is not the owner of the folder
     */
    @ApiMethod(name = "folders.transfer.list", httpMethod = HttpMethod.GET)
    public List<TransferRequest> getTransferRequests(@Named("folder") Long folder, User user)
            throws NotFoundException, ForbiddenException {
        // TODO
        return new ArrayList<>();
    }
}
