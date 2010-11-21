package redis.clients.johm.benchmark;

import redis.clients.johm.JOhmTestBase;

class JOhmBenchmarkTestBase extends JOhmTestBase {
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
