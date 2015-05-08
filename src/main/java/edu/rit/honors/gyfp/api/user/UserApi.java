package edu.rit.honors.gyfp.api.user;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
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
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.Permission;
import com.google.appengine.api.users.User;
import com.google.apphosting.api.ApiProxy;
import com.googlecode.objectify.ObjectifyService;
import edu.rit.honors.gyfp.api.ApiUtil;
import edu.rit.honors.gyfp.api.Constants;
import edu.rit.honors.gyfp.api.model.TransferRequest;
import edu.rit.honors.gyfp.api.model.TransferableFile;
import edu.rit.honors.gyfp.util.OfyService;
import edu.rit.honors.gyfp.util.Utils;

import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
public class UserApi {

    private static final Logger log = Logger.getLogger(UserApi.class.getName());

    /**
     * Incrementally completes a transfer request.
     * <p/>
     * Google drive has an API rate limit of 10 requests per second, and a maximum request duration of 60 seconds. At
     * the theoretical maximum, 600 requests could be completed in this time span, however in practice this value is
     * likely much lower. As such, an incremental approach is required to allow for such large requests. The returned
     * TransferRequest will contain the updated state after all requests that took place this session have completed.
     * <p/>
     * The limit parameter can be used to help implement a progress bar, as the total number of requests is known and
     * the number of requests per call is configurable. To get a higher precision, use a smaller limit.
     *
     * @param requestId
     *         The id of the transfer request
     * @param limit
     *         The maximum number of files to transfer during this request. This is required to ensure we do not exceed
     *         the rate limit imposed by the Drive API. If not specified, files will be transferred until the request is
     *         forced to end.
     * @param user
     *         The user who is completing the transfer request
     *
     * @return the current state of the transfer request, with successfully transfered files removed.
     *
     * @throws BadRequestException
     *         If the requested limit is greater than 600
     * @throws ForbiddenException
     *         If the user is not authorized to complete this transfer request
     * @throws NotFoundException
     *         If the transfer request cannot be found
     */
    @ApiMethod(name = "user.request.accept", httpMethod = HttpMethod.POST)
    public TransferRequest acceptRequest(@Named("request") long requestId, @Named("limit") @Nullable Integer limit, User user)
            throws BadRequestException, ForbiddenException, NotFoundException, InternalServerErrorException {

        TransferRequest request = getRequest(requestId, user);
        if (limit == null) {
            limit = 600;
        } else if (limit > 600 || limit <= 0) {
            throw new BadRequestException(String.format(Constants.Error.INVALID_TRANSFER_LIMIT, limit, 0, 600));
        }

        final Drive service = Utils.createDriveFromUser(user);
        final List<TransferableFile> success = new ArrayList<>();
        final Permission owner = new Permission();
        owner.setRole("owner");
        owner.setType("user");
        owner.setValue(request.getRequestingUser().getEmail());


        BatchRequest updateBatch = service.batch();
        final BatchRequest insertBatch = service.batch();

        for (final TransferableFile file : request.getFiles()) {

            try {
                service.permissions()
                        .update(file.getFileId(), request.getRequestingUser().getPermission(), owner)
                        .setTransferOwnership(true)
                        .queue(updateBatch, new JsonBatchCallback<Permission>() {
                    @Override
                    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                        log.log(Level.INFO, "Could not update ownership of file " + file.getFileId() + " (" + file.getFileName() + ").  Attempting to insert permission", e);
                        service.permissions().insert(file.getFileId(), owner).queue(insertBatch, new JsonBatchCallback<Permission>() {
                            @Override
                            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                                log.log(Level.SEVERE, "Could not transfer ownership of file " + file.getFileId() + " (" + file.getFileName() + ")", e);
                                log.log(Level.WARNING, e.getMessage() + ": " + e.getErrors());
                            }

                            @Override
                            public void onSuccess(Permission permission, HttpHeaders responseHeaders) throws IOException {
                                if (success.size() <= 5) {
                                    if (success.size() == 5) {
                                        log.info("...");
                                    } else {
                                        log.info("Transferred file " + file.getFileId() + " (" + file.getFileName() + ")");
                                    }
                                }
                                success.add(file);
                            }
                        });

                    }

                    @Override
                    public void onSuccess(Permission permission, HttpHeaders responseHeaders) throws IOException {
                        success.add(file);
                    }
                });
                success.add(file);
            } catch (IOException e) {
                throw new InternalServerErrorException(
                        Constants.Error.FAILED_DRIVE_REQUEST, e);
            }

            if (updateBatch.size() >= limit) {
                break;
            }
        }

        try {
            updateBatch.execute();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error executing batch request", e);
        }

        if (insertBatch.size() > 0) {
            log.log(Level.INFO, "There were failures.  Attempting to insert " + insertBatch.size() + " new permissions");
            try {
                insertBatch.execute();
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error executing batch request", e);
            }
        }

        request.getFiles().removeAll(success);

        // If there are no files left, delete the request, otherwise save it for future completion.
        if (request.getFiles().isEmpty()) {
            OfyService.ofy().delete().entity(request).now();
        } else {
            OfyService.ofy().save().entity(request).now();
        }
        OfyService.ofy().clear();
        return request;
    }


    /**
     * Returns details about the given transfer request, including a list of included files.
     *
     * @param requestId
     *         The ID of the request
     * @param user
     *         The user who is completing the transfer request
     *
     * @return The transfer request
     *
     * @throws ForbiddenException
     *         If the user does not own this request
     * @throws NotFoundException
     *         If the request cannot be found
     */
    @ApiMethod(name = "user.request.get", httpMethod = HttpMethod.GET)
    public TransferRequest getRequest(@Named("request") long requestId, User user)
            throws ForbiddenException, NotFoundException {
        if (user == null) {
            throw new ForbiddenException(Constants.Error.AUTH_REQUIRED);
        }
        TransferRequest request = OfyService.ofy().load().type(TransferRequest.class).id(requestId).now();
        OfyService.ofy().clear();

        if (request == null) {
            throw new NotFoundException(String.format(Constants.Error.TRANSFER_REQUEST_NOT_FOUND, requestId));
        }

        if (request.getTargetUser().getEmail().toLowerCase().equals(user.getEmail().toLowerCase())) {
            log.info(request.getTargetUser() + " == " + user.getUserId());
            return request;
        } else {
            log.info(request.getTargetUser().getEmail() + " != " + user.getEmail());
            throw new ForbiddenException(Constants.Error.TRANSFER_REQUEST_INCORRECT_USER);
        }
    }

    /**
     * Allows users to selectively exclude files from the transfer request.
     *
     * @param requestId
     *         The id of the transfer request being modified
     * @param ids
     *         The ids of the files that should not be included in the request
     * @param user
     *         The user who is completing the transfer request
     *
     * @throws ForbiddenException
     *         If the user does not own this request
     * @throws NotFoundException
     *         If the request cannot be found
     * @throws BadRequestException
     *         If any of the given file IDs are not part of the request. The valid files that were found, however, will
     *         still be removed even if this exception is thrown.
     */
    @ApiMethod(name = "user.request.remove", httpMethod = HttpMethod.POST)
    public void removeFilesFromTransfer(@Named("request") long requestId, @Named("ids") Set<String> ids, User user)
            throws ForbiddenException, NotFoundException, BadRequestException {
        TransferRequest request = getRequest(requestId, user);

        ApiUtil.splitStringArguments(ids);
        log.info("Request started with " + request.getFiles().size() + " files.");
        log.info("Processing id list of size " + ids.size());

        List<TransferableFile> toRemove = new ArrayList<>();
        for (TransferableFile file : request.getFiles()) {
            if (ids.contains(file.getFileId())) {
                toRemove.add(file);
                ids.remove(file.getFileId());

                if (ids.size() == 0) {
                    break;
                }
            }
        }

        request.getFiles().removeAll(toRemove);
        log.info("Request now contains " + request.getFiles().size() + " files.");
        OfyService.ofy().save().entity(request).now();
        OfyService.ofy().clear();
        if (!ids.isEmpty()) {
            throw new BadRequestException(String.format(Constants.Error.REMOVE_UNKNOWN_FILE_IDS, ids));
        }
    }


    /**
     * Deletes a transfer request
     *
     * @param requestId
     *         The ids of the transfer request to remove
     * @param user
     *         The user who is completing the transfer request
     *
     * @throws ForbiddenException
     *         If the user does not own this request
     * @throws NotFoundException
     *         If the request cannot be found
     * @throws BadRequestException
     *         If any of the given file IDs are not part of the request. The valid files that were found, however, will
     *         still be removed even if this exception is thrown.
     */
    @ApiMethod(name = "user.request.delete", httpMethod = HttpMethod.POST)
    public void delete(@Named("request") long requestId, User user)
            throws ForbiddenException, NotFoundException, BadRequestException {
        TransferRequest request = getRequest(requestId, user);

        OfyService.ofy().delete().entity(request).now();
        OfyService.ofy().clear();
    }

    public static class VerificationResult {
        private boolean success;

        private VerificationResult(boolean success) {
            this.success = success;
        }

        public boolean getSuccess() {
            return success;
        }

    }

    public static final VerificationResult PASS = new VerificationResult(true);
    public static final VerificationResult FAIL = new VerificationResult(false);

    @ApiMethod(name = "user.verify.installation", httpMethod = HttpMethod.GET)
    public VerificationResult verifyInstallation(User user) {
        try {
            Drive service = Utils.createDriveFromUser(user);
            About execute = service.about().get().execute();
            return PASS;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error verifying user", e);
            return FAIL;
        }
    }
}
