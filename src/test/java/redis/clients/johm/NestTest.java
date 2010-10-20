package redis.clients.johm;

import java.util.concurrent.TimeoutException;

import org.junit.Test;

import redis.clients.jedis.Jedis;

public class NestTest extends JOhmTestBase {

    @SuppressWarnings("unchecked")
    @Test
    public void checkKeyGeneration() throws TimeoutException {
        Nest users = new Nest("users");
        users.setJedisPool(jedisPool);
        assertEquals("users", users.key());
        assertEquals("users:123", users.cat(123).key());
        assertEquals("users:123:name", users.cat(123).cat("name").key());

        users.cat(123).cat("name").set("foo");
        Jedis jedis = jedisPool.getResource();
        assertEquals("foo", jedis.get("users:123:name"));
        jedisPool.returnBrokenResource(jedis);
        assertEquals("foo", users.cat(123).cat("name").get());
    }
}