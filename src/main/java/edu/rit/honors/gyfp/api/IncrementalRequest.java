package edu.rit.honors.gyfp.api;

import com.google.api.services.drive.Drive;

import java.io.IOException;

/**
 * Created by Greg on 11/10/2014.
 */
public interface IncrementalRequest<T> {

    public boolean hasNext();
    public void failed(T failed);
    public void passed(T passed);
    public T next();
    public void execute(Drive service) throws IOException;
    public void save();

}
