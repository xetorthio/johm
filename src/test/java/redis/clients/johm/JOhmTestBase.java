package redis.clients.johm;

import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JOhmTestBase extends Assert {
    protected static JedisPool jedisPool;

    @Before
    public void startUp() throws TimeoutException {
	jedisPool = new JedisPool("localhost");
	jedisPool.init();
	JOhm.setPool(jedisPool);

	Jedis jedis = jedisPool.getResource();
	jedis.flushAll();
	jedisPool.returnResource(jedis);
    }

    @After
    public void after() {
	jedisPool.destroy();
    }
}
