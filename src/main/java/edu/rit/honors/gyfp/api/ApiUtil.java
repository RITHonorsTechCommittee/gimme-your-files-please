package edu.rit.honors.gyfp.api;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveRequest;
import com.google.apphosting.api.ApiProxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;

public class ApiUtil {

    private static final Logger log = Logger.getLogger(ApiUtil.class.getName());
    /**
     * Executes a long series of request in one API call.
     *
     * Google App Engine and the Drive API have usage limits built in.  This method is our way around them.  It takes a request queue object, which knows how to generate and execute the requests necessary to preform the entire operation and executes as many iterations as possible in a single request.
     *
     * The basic procedure is as follows:
     *   * Check if there is an additional item to process in the queue.
     *   * Check that we still want to process items.  maxRequests limits the
     *     number of requests that can be made in one api call
     *   * Check that there is still a reasonable amount of time remaining for
     *     the request.  Google App Engine requests are allowed to take 60
     *     seconds at most.  Fortunately we can query for how much time
     *     remains.  Some amount of time must be saved for cleanup to ensure
     *     that completed work can be persisted.
     *
     *     * Attempt to process the item:
     *       *  If the request method completes successfully, register the item
     *          as a success.
     *       *  If the request fails, register the item as failed.
     *     * Sleep for a short period to ensure that per second api limits are
     *       not exceeded.  Google Drive's API is limited to 10 requests per
     *       second.
     *
     * @param itemSource
     *              The source of all items and the processor of each request
     * @param executor
     *              The executor which is able to
     * @param maxRequests
     *              The maximum number of requests to execute in this session
     * @param <T>
     *              The type of item on which the requestQueue operates
     * @throws InternalServerErrorException
     */
    public static <T> void safeExecuteDriveRequestQueue(Iterable<T> itemSource, IncrementalExecutor<T> executor, int maxRequests) throws InternalServerErrorException {
        checkArgument(maxRequests > 0 && maxRequests <= 500);

        Iterator<T> items = itemSource.iterator();

        while (maxRequests > 0 && items.hasNext() &&
                ApiProxy.getCurrentEnvironment().getRemainingMillis() >= 3000) {
            T item = items.next();
            try {
                if (executor.execute(item)) {
                    items.remove();
                }
                // Maybe not necessary, but just to be safe slow ourselves down
                // slightly to avoid hitting the rate limit
                Thread.sleep(50);
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "Interrupted during sleep");
                throw new InternalServerErrorException(
                        Constants.Error.SLEEP_INTERRUPTED, e);
            }
        }
    }
}
