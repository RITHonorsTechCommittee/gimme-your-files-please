package edu.rit.honors.gyfp.util;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import edu.rit.honors.gyfp.model.DriveUser;


public class OfyService {
    static {
    	System.out.println("Registering classes");
        ObjectifyService.register(DriveUser.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

}