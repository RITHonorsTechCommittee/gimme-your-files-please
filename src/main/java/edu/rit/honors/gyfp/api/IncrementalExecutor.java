package edu.rit.honors.gyfp.api;

/**
 * Interface that provides a mechanism for incrementally executing a long-running operation.
 *
 * @param <T>
 *         The type of data that is being operated over.  For example, a File
 */
public interface IncrementalExecutor<T> {

    public boolean execute(T item);

}
