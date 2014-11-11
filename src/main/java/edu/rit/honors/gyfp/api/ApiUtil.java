package edu.rit.honors.gyfp.api;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveRequest;
import com.google.apphosting.api.ApiProxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class ApiUtil {
    public static <T> void safeExecuteDriveRequestQueue(Drive service, IncrementalRequest<T> requestQueue, int maxRequests) throws InternalServerErrorException {
        checkArgument(maxRequests > 0 && maxRequests <= 500);

        while (maxRequests > 0 && requestQueue.hasNext() &&
                ApiProxy.getCurrentEnvironment().getRemainingMillis() >= 3000) {
            T item = null;

            try {
                item = requestQueue.next();
                requestQueue.execute(service);
                requestQueue.passed(item);
                // Maybe not necessary, but just to be safe slow ourselves down slightly to avoid hitting the rate limit
                Thread.sleep(50);
            } catch (IOException e) {
                requestQueue.failed(item);
                throw new InternalServerErrorException(
                        Constants.Error.FAILED_DRIVE_REQUEST, e);
            } catch (InterruptedException e) {
                throw new InternalServerErrorException(
                        Constants.Error.SLEEP_INTERRUPTED, e);
            }
        }

        requestQueue.save();
    }
}
