package redis.clients.johm;

import org.junit.Assert;
import org.junit.Before;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class JOhmTestBase extends Assert {
    protected JedisPool jedisPool;
    protected volatile static boolean benchmarkMode;

    @Before
    public void startUp() {
        startJedisEngine();
    }

    protected void startJedisEngine() {
        if (benchmarkMode) {
            jedisPool = new JedisPool(new JedisPoolConfig(), "192.168.1.109",
                    Protocol.DEFAULT_PORT, 2000, "redismaster");
        } else {
            jedisPool = new JedisPool(new JedisPoolConfig(), "192.168.1.109", Protocol.DEFAULT_PORT, 0, "redismaster");
        }
        JOhm.setPool(jedisPool);
        purgeRedis();
    }

    protected void purgeRedis() {
        Jedis jedis = jedisPool.getResource();
        jedis.flushAll();
        jedisPool.returnResource(jedis);
    }
}