package redis.clients.johm.benchmark;

import org.junit.Test;

import redis.clients.johm.JOhm;
import redis.clients.johm.models.User;

public class SaveSearchBenchmark extends JOhmBenchmarkTestBase {
    @Test
    public void saveSearchModel() {
        int totalOps = 5000;
        timer.begin();
        User user = null;
        for (int n = 0; n < totalOps; n++) {
            user = new User();
            user.setName("foo" + n);
            user.setRoom("vroom" + n);
            user.setAge(n);
            user.setSalary(9999.99f);
            user.setInitial('f');
            JOhm.save(user);
            JOhm.find(User.class, "name", "foo" + n);
            JOhm.find(User.class, "age", n);
        }
        timer.end();
        printStats("saveSearchModel", totalOps, 3, timer.elapsed());
    }
}
