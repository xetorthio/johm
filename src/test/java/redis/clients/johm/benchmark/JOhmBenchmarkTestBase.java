package redis.clients.johm.benchmark;

import java.io.IOException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.johm.JOhm;
import redis.clients.johm.JOhmTestBase;

class JOhmBenchmarkTestBase extends JOhmTestBase {
    protected final SimpleTimer timer = new SimpleTimer();
    protected volatile Jedis cachedConnection;

    {
        benchmarkMode = true;
    }

    protected synchronized void reuseModeBegin() {
        System.out.println("Reuse-mode coming up");
        jedisPool.destroy();
        benchmarkMode = false;
        JOhm.setReuseConnectionMode(true, getHandle());
    }

    protected synchronized void reuseModeEnd() {
        JOhm.setReuseConnectionMode(false, null);
        removeHandle();
        benchmarkMode = true;
        System.out.println("Reuse-mode torn down");
    }

    private synchronized Jedis getHandle() {
        if (cachedConnection == null || !cachedConnection.isConnected()) {
            cachedConnection = new Jedis("localhost", Protocol.DEFAULT_PORT);
            try {
                cachedConnection.connect();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return cachedConnection;
    }

    private synchronized void removeHandle() {
        if (cachedConnection != null) {
            cachedConnection.quit();
            try {
                cachedConnection.disconnect();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                cachedConnection = null;
            }
        }
    }

    protected void printStats(String test, int totalOps, int opTypes,
            long elapsed) {
        StringBuilder stats = new StringBuilder();
        stats.append("[").append(test).append("]");
        stats.append(" totalOps=").append((opTypes * totalOps));
        stats.append(", opTypes=").append(opTypes);
        stats.append(", thruput=")
                .append((1000 * opTypes * totalOps) / elapsed).append(
                        " ops");
        System.out.println(stats);
    }
}
