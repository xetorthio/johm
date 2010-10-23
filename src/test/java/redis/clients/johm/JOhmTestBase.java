package redis.clients.johm;

import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

public class JOhmTestBase extends Assert {
    protected JedisPool jedisPool;
    protected volatile static boolean benchmarkMode;

    @Before
    public void startUp() throws TimeoutException {
        startJedisEngine();
    }

    protected void startJedisEngine() throws TimeoutException {
        if (benchmarkMode) {
            jedisPool = new JedisPool("localhost", Protocol.DEFAULT_PORT, 2000);
            jedisPool.setResourcesNumber(50);
            jedisPool.setDefaultPoolWait(1000000);
        } else {
            jedisPool = new JedisPool("localhost");
        }
        jedisPool.init();
        JOhm.setPool(jedisPool);
        purgeRedis();
    }

    protected void purgeRedis() throws TimeoutException {
        Jedis jedis = jedisPool.getResource();
        jedis.flushAll();
        jedisPool.returnResource(jedis);
    }
}