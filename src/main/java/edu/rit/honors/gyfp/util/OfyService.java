package edu.rit.honors.gyfp.util;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import edu.rit.honors.gyfp.api.model.FileUser;
import edu.rit.honors.gyfp.api.model.Folder;
import edu.rit.honors.gyfp.api.model.TransferRequest;
import edu.rit.honors.gyfp.api.model.TransferableFile;
import edu.rit.honors.gyfp.model.DriveUser;


public class OfyService {
    static {
        System.out.println("Registering classes");
        ObjectifyService.register(DriveUser.class);
        ObjectifyService.register(Folder.class);
        ObjectifyService.register(FileUser.class);
        ObjectifyService.register(TransferableFile.class);
        ObjectifyService.register(TransferRequest.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

}