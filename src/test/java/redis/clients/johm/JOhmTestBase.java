package redis.clients.johm;

import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JOhmTestBase extends Assert {
    protected static JedisPool jedisPool;

    @BeforeClass
    public static void before() {
	jedisPool = new JedisPool("localhost");
	jedisPool.init();
	JOhm.setPool(jedisPool);
    }

    @Before
    public void startUp() throws TimeoutException {
	Jedis jedis = jedisPool.getResource();
	jedis.flushAll();
	jedisPool.returnResource(jedis);
    }

    @AfterClass
    public static void after() {
	jedisPool.destroy();
    }
}
