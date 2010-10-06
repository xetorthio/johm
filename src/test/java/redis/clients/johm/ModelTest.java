package redis.clients.johm;

import java.util.List;

import org.junit.Test;

import redis.clients.johm.models.Country;
import redis.clients.johm.models.User;

public class ModelTest extends JOhmTestBase {
    @Test
    public void checkModelPersistence() {
        User user = new User();
        user.setName("foo");
        user.setRoom("vroom");
        user = user.save();

        assertNotNull(user);
        // assertEquals(11, user.getId());
        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(user.getName(), savedUser.getName());
        assertNull(savedUser.getRoom());
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getAge(), savedUser.getAge());

        // cleanup now
        assertTrue(user.delete());
        assertNull(JOhm.get(User.class, user.getId()));
    }

    @Test
    public void checkModelPersistenceOtherValueTypes() {
        User user1 = new User();
        user1.setName("foo");
        user1.setRoom("vroom");
        user1.setAge(99);
        user1.setSalary(9999.99f);
        user1.setInitial('f');
        user1 = user1.save();

        User user2 = new User();
        user2.setName("foo2");
        user2.setRoom("vroom2");
        user2.setAge(9);
        user2.setInitial('f');
        user2 = user2.save();

        User user3 = new User();
        user3.setName("foo3");
        user3.setRoom("vroom3");
        user3.setAge(19);
        user3.setSalary(9999.9f);
        user3.setInitial('f');
        user3 = user3.save();

        assertNotNull(user1);
        // assertEquals(1, user1.getId());
        User savedUser1 = JOhm.get(User.class, user1.getId());
        assertEquals(user1.getName(), savedUser1.getName());
        assertNull(savedUser1.getRoom());
        assertEquals(user1.getId(), savedUser1.getId());
        assertEquals(user1.getAge(), savedUser1.getAge());
        assertEquals(user1.getInitial(), savedUser1.getInitial());
        assertEquals(user1.getSalary(), savedUser1.getSalary(), 0D);

        assertNotNull(user2);
        // assertEquals(2, user2.getId());
        User savedUser2 = JOhm.get(User.class, user2.getId());
        assertEquals(user2.getName(), savedUser2.getName());
        assertNull(savedUser2.getRoom());
        assertEquals(user2.getId(), savedUser2.getId());
        assertEquals(user2.getInitial(), savedUser2.getInitial());
        assertEquals(user2.getAge(), savedUser2.getAge());

        assertNotNull(user3);
        // assertEquals(3, user3.getId());
        User savedUser3 = JOhm.get(User.class, user3.getId());
        assertEquals(user3.getName(), savedUser3.getName());
        assertNull(savedUser3.getRoom());
        assertEquals(user3.getId(), savedUser3.getId());
        assertEquals(user3.getAge(), savedUser3.getAge());
        assertEquals(user3.getInitial(), savedUser3.getInitial());
        assertEquals(user3.getSalary(), savedUser3.getSalary(), 0D);

        // cleanup now
        assertTrue(user1.delete());
        assertNull(JOhm.get(User.class, user1.getId()));
        assertTrue(user2.delete());
        assertNull(JOhm.get(User.class, user2.getId()));
        assertTrue(user3.delete());
        assertNull(JOhm.get(User.class, user3.getId()));
    }

    @Test
    public void checkModelDeletion() {
        User user = new User();
        user.save();
        int id = user.getId();

        assertNotNull(JOhm.get(User.class, id));
        assertTrue(user.delete());
        assertNull(JOhm.get(User.class, id));

        user = new User();
        user.save();
        id = user.getId();

        assertNotNull(JOhm.get(User.class, id));
        assertTrue(user.delete());
        assertNull(JOhm.get(User.class, id));
    }

    @Test(expected = InvalidFieldException.class)
    public void cannotSearchOnNullField() {
        User user1 = new User();
        user1.setName("model1");
        user1.setRoom("tworoom");
        user1.setAge(88);
        user1.save();

        JOhm.find(User.class, null, "foo");
    }

    @Test(expected = InvalidFieldException.class)
    public void cannotSearchWithNullValue() {
        User user1 = new User();
        user1.setName("model1");
        user1.setRoom("tworoom");
        user1.setAge(88);
        user1.save();

        JOhm.find(User.class, "age", null);
    }

    @Test(expected = InvalidFieldException.class)
    public void cannotSearchWithOnNotIndexedFields() {
        User user1 = new User();
        user1.setName("model1");
        user1.setRoom("tworoom");
        user1.setAge(88);
        user1.save();

        JOhm.find(User.class, "salary", 1000);
    }

    @Test
    public void checkModelSearch() {
        User user1 = new User();
        user1.setName("model1");
        user1.setRoom("tworoom");
        user1.setAge(88);
        user1.setSalary(9999.99f);
        user1.setInitial('m');
        user1.save();
        int id1 = user1.getId();

        User user2 = new User();
        user2.setName("zmodel2");
        user2.setRoom("threeroom");
        user2.setAge(8);
        user2.setInitial('z');
        user2 = user2.save();
        int id2 = user2.getId();

        assertNotNull(JOhm.get(User.class, id1));
        assertNotNull(JOhm.get(User.class, id2));

        List<User> users = JOhm.find(User.class, "age", 88);
        assertEquals(1, users.size());
        User user1Found = users.get(0);
        assertEquals(user1Found.getAge(), user1.getAge());
        assertEquals(user1Found.getName(), user1.getName());
        assertNull(user1Found.getRoom());
        assertEquals(user1Found.getSalary(), user1.getSalary(), 0D);
        assertEquals(user1Found.getInitial(), user1.getInitial());

        users = JOhm.find(User.class, "age", 8);
        assertEquals(1, users.size());
        User user2Found = users.get(0);
        assertEquals(user2Found.getAge(), user2.getAge());
        assertEquals(user2Found.getName(), user2.getName());
        assertNull(user2Found.getRoom());
        assertEquals(user2Found.getSalary(), user2.getSalary(), 0D);
        assertEquals(user2Found.getInitial(), user2.getInitial());

        users = JOhm.find(User.class, "name", "model1");
        assertEquals(1, users.size());
        User user3Found = users.get(0);
        assertEquals(user3Found.getAge(), user1.getAge());
        assertEquals(user3Found.getName(), user1.getName());
        assertNull(user3Found.getRoom());
        assertEquals(user3Found.getSalary(), user1.getSalary(), 0D);
        assertEquals(user3Found.getInitial(), user1.getInitial());

        users = JOhm.find(User.class, "name", "zmodel2");
        assertEquals(1, users.size());
        User user4Found = users.get(0);
        assertEquals(user4Found.getAge(), user2.getAge());
        assertEquals(user4Found.getName(), user2.getName());
        assertNull(user4Found.getRoom());
        assertEquals(user4Found.getSalary(), user2.getSalary(), 0D);
        assertEquals(user4Found.getInitial(), user2.getInitial());
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

        // cleanup now
        assertTrue(user.delete());
    }
}