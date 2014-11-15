package edu.rit.honors.gyfp.api;

import com.google.api.services.drive.Drive;

import java.io.IOException;

/**
 * Created by Greg on 11/10/2014.
 */
public interface IncrementalExecutor<T> {

    public boolean execute(T item);

}
