package redis.clients.johm;

import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JOhmTestBase extends Assert {
    protected static JedisPool jedisPool;

    @Before
    public void startUp() throws TimeoutException {
        startJedisEngine();
    }

    protected static void startJedisEngine() throws TimeoutException {
        jedisPool = new JedisPool("localhost", 6379, 2000);
        jedisPool.init();
        JOhm.setPool(jedisPool);
        purgeRedis();
    }

    protected static void purgeRedis() throws TimeoutException {
        Jedis jedis = jedisPool.getResource();
        jedis.select(0);
        jedis.flushDB();
        jedisPool.returnResource(jedis);
    }
}