package redis.clients.johm.collections;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class provides the common contract for asynchronous operations on
 * internal storage management of all JOhm collections.
 */
abstract class RedisBaseCollection {
    // The choice of single-threaded executor is driven by current use of
    // non-thread-safe internal data store collections.
    protected final ExecutorService executor = Executors
            .newSingleThreadExecutor();
    protected volatile boolean supportsAsyncMode = true;

    /**
     * Refresh the internal proxy data store of a RedisBaseCollection
     * implementation from the authoritative Redis data store.
     * 
     * If asynchronous mode is selected and supported by the implementing
     * collection, refreshStorage() will happen in the background without
     * causing delay to users of a collection API that involves internal
     * refresh, and the API method will return instantly as soon as the bare
     * minimum remote work is completed.
     * 
     * @param asynchronousOperation
     */
    protected synchronized void refreshStorage(boolean asynchronousOperation) {
        if (asynchronousOperation && supportsAsyncMode) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    purgeScrollStorage();
                }
            });
        } else {
            purgeScrollStorage();
        }
    }

    protected abstract void purgeScrollStorage();

    public void switchOffAsynchrony() {
        supportsAsyncMode = false;
    }

    private void shutdownAndAwaitTermination() {
        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdownAndAwaitTermination();
            }
        });
    }

}
