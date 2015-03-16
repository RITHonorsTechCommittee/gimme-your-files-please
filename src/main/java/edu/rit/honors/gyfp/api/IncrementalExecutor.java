package edu.rit.honors.gyfp.api;

/**
 * Created by Greg on 11/10/2014.
 */
public interface IncrementalExecutor<T> {

    public boolean execute(T item);

}
