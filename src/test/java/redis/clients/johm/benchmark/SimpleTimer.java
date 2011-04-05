package redis.clients.johm.benchmark;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;

final class SimpleTimer {
    private final AtomicLong beginMillis = new AtomicLong(0L);
    private final AtomicLong endMillis = new AtomicLong(0L);

    void begin() {
        reset(); // better be safe
        beginMillis.set(Calendar.getInstance().getTimeInMillis());
    }

    void end() {
        endMillis.set(Calendar.getInstance().getTimeInMillis());
    }

    long elapsed() {
        return endMillis.get() - beginMillis.get();
    }

    void reset() {
        beginMillis.set(0L);
        endMillis.set(0L);
    }
}
