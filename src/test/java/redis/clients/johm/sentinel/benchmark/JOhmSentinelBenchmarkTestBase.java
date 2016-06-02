package redis.clients.johm.sentinel.benchmark;

import redis.clients.johm.sentinel.JOhmTestSentinelBase;

class JOhmSentinelBenchmarkTestBase extends JOhmTestSentinelBase {
    protected final SimpleTimer timer = new SimpleTimer();

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
