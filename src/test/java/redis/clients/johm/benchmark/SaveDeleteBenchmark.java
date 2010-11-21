package redis.clients.johm.benchmark;

import org.junit.Test;

import redis.clients.johm.JOhm;
import redis.clients.johm.models.User;

public class SaveDeleteBenchmark extends JOhmBenchmarkTestBase {
    @Test
    public void saveDeleteModel() {
        int totalOps = 5000;
        timer.begin();
        User user = null;
        for (int n = 0; n < totalOps; n++) {
            user = new User();
            user.setName("foo" + n);
            user.setRoom("vroom" + n);
            user.setAge(99);
            user.setSalary(9999.99f);
            user.setInitial('f');
            JOhm.save(user);
            JOhm.delete(User.class, user.getId());
        }
        timer.end();
        printStats("saveDeleteModel", totalOps, 2, timer.elapsed());
    }
}
