package redis.clients.johm;

import org.junit.Test;

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

}