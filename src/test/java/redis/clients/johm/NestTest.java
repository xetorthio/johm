package redis.clients.johm;

import java.util.concurrent.TimeoutException;

import org.junit.Test;

public class NestTest extends JOhmTestBase {

    @Test
    public void checkKeyGeneration() throws TimeoutException {
	Nest users = new Nest("users", jedisPool);
	assertEquals("users", users.key());
	assertEquals("users:123", users.cat(123).key());
	assertEquals("users:123:name", users.cat(123).cat("name").key());

	users.cat(123).cat("name").set("foo");
	assertEquals("foo", jedisPool.getResource().get("users:123:name"));
	assertEquals("foo", users.cat(123).cat("name").get());
    }
}