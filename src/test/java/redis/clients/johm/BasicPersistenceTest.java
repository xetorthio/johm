package redis.clients.johm;

import java.util.Set;

import org.junit.Test;

import redis.clients.johm.models.Country;
import redis.clients.johm.models.FaultyModel;
import redis.clients.johm.models.Item;
import redis.clients.johm.models.User;

public class BasicPersistenceTest extends JOhmTestBase {
    @Test
    public void save() {
        User user = new User();
        user.setName("foo");
        user.setRoom("vroom");
        user = JOhm.save(user);

        assertNotNull(user);
        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(user.getName(), savedUser.getName());
        assertNull(savedUser.getRoom());
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getAge(), savedUser.getAge());
    }

    @Test
    public void saveWithArray() {
        Item item0 = new Item();
        item0.setName("Foo0");
        JOhm.save(item0);

        Item item1 = new Item();
        item1.setName("Foo1");
        JOhm.save(item1);

        Item item2 = new Item();
        item2.setName("Foo2");
        JOhm.save(item2);

        User user = new User();
        user.setName("foo");
        user.setRoom("vroom");
        user.setThreeLatestPurchases(new Item[] { item0, item1, item2 });
        user = JOhm.save(user);

        assertNotNull(user);
        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(user.getName(), savedUser.getName());
        assertNull(savedUser.getRoom());
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getAge(), savedUser.getAge());

        Item[] saved = savedUser.getThreeLatestPurchases();
        assertEquals(3, saved.length);
        assertEquals(item0.getId(), saved[0].getId());
        assertEquals(item0.getName(), saved[0].getName());
        assertEquals(item1.getId(), saved[1].getId());
        assertEquals(item1.getName(), saved[1].getName());
        assertEquals(item2.getId(), saved[2].getId());
        assertEquals(item2.getName(), saved[2].getName());

        assertTrue(JOhm.delete(User.class, savedUser.getId(), true, true));
        assertTrue(JOhm.delete(Item.class, item0.getId()));
        assertTrue(JOhm.delete(Item.class, item1.getId()));
        assertTrue(JOhm.delete(Item.class, item2.getId()));
    }

    @Test
    public void saveWithOtherValueTypes() {
        User user1 = new User();
        user1.setName("foo");
        user1.setRoom("vroom");
        user1.setAge(99);
        user1.setSalary(9999.99f);
        user1.setInitial('f');
        user1 = JOhm.save(user1);

        User user2 = new User();
        user2.setName("foo2");
        user2.setRoom("vroom2");
        user2.setAge(9);
        user2.setInitial('f');
        user2 = JOhm.save(user2);

        User user3 = new User();
        user3.setName("foo3");
        user3.setRoom("vroom3");
        user3.setAge(19);
        user3.setSalary(9999.9f);
        user3.setInitial('f');
        user3 = JOhm.save(user3);

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
        assertTrue(JOhm.delete(User.class, user1.getId()));
        assertNull(JOhm.get(User.class, user1.getId()));
        assertTrue(JOhm.delete(User.class, user2.getId()));
        assertNull(JOhm.get(User.class, user2.getId()));
        assertTrue(JOhm.delete(User.class, user3.getId()));
        assertNull(JOhm.get(User.class, user3.getId()));
    }

    @Test
    public void delete() {
        User user = new User();
        JOhm.save(user);
        Long id = user.getId();

        assertNotNull(JOhm.get(User.class, id));
        assertTrue(JOhm.delete(User.class, id));
        assertNull(JOhm.get(User.class, id));

        user = new User();
        JOhm.save(user);
        id = user.getId();

        assertNotNull(JOhm.get(User.class, id));
        assertTrue(JOhm.delete(User.class, id));
        assertNull(JOhm.get(User.class, id));
    }

    @Test
    public void shouldNotPersistFieldsWithoutAttributeAnnotation() {
        User user = new User();
        user.setName("foo");
        user.setRoom("3A");
        JOhm.save(user);

        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(user.getName(), savedUser.getName());
        assertNull(savedUser.getRoom());
    }

    @Test(expected = MissingIdException.class)
    public void shouldFailWhenReferenceWasNotSaved() {
        User user = new User();
        user.setName("bar");
        user.setCountry(new Country());
        JOhm.save(user);
    }

    @Test(expected = JOhmException.class)
    public void shouldNotPersistWithoutModel() {
        Nest<String> dummyNest = new Nest<String>();
        JOhm.save(dummyNest);
    }

    @Test(expected = JOhmException.class)
    public void shouldNotPersistModelWithOtherJOhmIdAnnotations() {
        FaultyModel badModel = new FaultyModel();
        badModel.setType("horribleId");
        JOhm.save(badModel);
    }

    @Test
    public void shouldHandleReferences() {
        User user = new User();
        user.setName("foo");
        user.setRoom("3A");
        JOhm.save(user);

        User savedUser = JOhm.get(User.class, user.getId());
        assertNull(savedUser.getCountry());

        Country somewhere = new Country();
        somewhere.setName("Somewhere");
        JOhm.save(somewhere);

        user = new User();
        user.setName("bar");
        user.setCountry(somewhere);
        JOhm.save(user);

        savedUser = JOhm.get(User.class, user.getId());
        assertNotNull(savedUser.getCountry());
        assertEquals(somewhere.getId(), savedUser.getCountry().getId());
        assertEquals(somewhere.getName(), savedUser.getCountry().getName());
    }

    @Test
    public void getAll() {
        User user = new User();
        user.setName("foo");
        JOhm.save(user);
        user = new User();
        user.setName("foo1");
        JOhm.save(user);

        Set<User> users = JOhm.getAll(User.class);
        assertEquals(2, users.size());
    }
}