package redis.clients.johm.benchmark;

import org.junit.Test;

import redis.clients.johm.JOhm;
import redis.clients.johm.models.User;

public class SaveGetBenchmark extends JOhmBenchmarkTestBase {
    @Test
    public void saveGetModel() {
        int totalOps = 5000;
        timer.begin();
        for (int n = 0; n < totalOps; n++) {
            User user = new User();
            user.setName("foo" + n);
            user.setRoom("vroom" + n);
            JOhm.save(user);
            JOhm.get(User.class, user.getId());
        }
        timer.end();
        printStats("saveGetModel", totalOps, 2, timer.elapsed());
    }
}
