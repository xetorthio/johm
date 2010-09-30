package redis.clients.johm;

import org.junit.Test;

import redis.clients.johm.models.Country;
import redis.clients.johm.models.User;

public class ModelTest extends JOhmTestBase {

    @Test(expected = MissingIdException.class)
    public void cannotGetIdWhenNew() {
	User user = new User();
	user.getId();
    }

    @Test
    public void checkModelPersistence() {
	User user = new User();
	user.setName("foo");
	user = user.save();

	assertNotNull(user);
	assertEquals(1, user.getId());
	User savedUser = JOhm.get(User.class, user.getId());
	assertEquals(user.getName(), savedUser.getName());
	assertEquals(user.getId(), savedUser.getId());
    }

    @Test
    public void shouldNotPersistFieldsWithoutAttributeAnnotation() {
	User user = new User();
	user.setName("foo");
	user.setRoom("3A");
	user.save();

	User savedUser = JOhm.get(User.class, user.getId());
	assertEquals(user.getName(), savedUser.getName());
	assertNull(savedUser.getRoom());
    }

    @Test(expected = MissingIdException.class)
    public void shouldFailWhenReferenceWasNotSaved() {
	User user = new User();
	user.setName("bar");
	user.setCountry(new Country());
	user.save();
    }

    @Test
    public void shouldHandleReferences() {
	User user = new User();
	user.setName("foo");
	user.setRoom("3A");
	user.save();

	System.out.println(user.getId());
	User savedUser = JOhm.get(User.class, user.getId());
	assertNull(savedUser.getCountry());

	Country somewhere = new Country();
	somewhere.setName("Somewhere");
	somewhere.save();

	user = new User();
	user.setName("bar");
	user.setCountry(somewhere);
	user.save();

	savedUser = JOhm.get(User.class, user.getId());
	assertNotNull(savedUser.getCountry());
	assertEquals(somewhere.getId(), savedUser.getCountry().getId());
	assertEquals(somewhere.getName(), savedUser.getCountry().getName());
    }
}