package redis.clients.johm;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import redis.clients.johm.models.Country;
import redis.clients.johm.models.Item;
import redis.clients.johm.models.User;

public class SearchTest extends JOhmTestBase {
    @Test(expected = InvalidFieldException.class)
    public void cannotSearchOnNullField() {
        User user1 = new User();
        user1.setName("model1");
        user1.setRoom("tworoom");
        user1.setAge(88);
        JOhm.save(user1);

        JOhm.find(User.class, null, "foo");
    }

    @Test(expected = InvalidFieldException.class)
    public void cannotSearchWithNullValue() {
        User user1 = new User();
        user1.setName("model1");
        user1.setRoom("tworoom");
        user1.setAge(88);
        JOhm.save(user1);

        JOhm.find(User.class, "age", null);
    }

    @Test(expected = InvalidFieldException.class)
    public void cannotSearchWithOnNotIndexedFields() {
        User user1 = new User();
        user1.setName("model1");
        user1.setRoom("tworoom");
        user1.setAge(88);
        JOhm.save(user1);

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
        JOhm.save(user1);
        Long id1 = user1.getId();

        User user2 = new User();
        user2.setName("zmodel2");
        user2.setRoom("threeroom");
        user2.setAge(8);
        user2.setInitial('z');
        user2 = JOhm.save(user2);
        Long id2 = user2.getId();

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
    public void canSearchOnLists() {
        Item item = new Item();
        item.setName("bar");
        JOhm.save(item);

        User user1 = new User();
        user1.setName("foo");
        JOhm.save(user1);
        user1.getLikes().add(item);

        User user2 = new User();
        user2.setName("car");
        JOhm.save(user2);
        user2.getLikes().add(item);

        List<User> users = JOhm.find(User.class, "likes", item.getId());

        assertEquals(2, users.size());
        Long[] controlSet = { user1.getId(), user2.getId() };
        assertTrue(users.get(0).getId() != users.get(1).getId());
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(0).getId()) >= 0);
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(1).getId()) >= 0);
    }

    @Test
    public void canSearchOnArrays() {
        Item item0 = new Item();
        item0.setName("Foo0");
        JOhm.save(item0);

        Item item1 = new Item();
        item1.setName("Foo1");
        JOhm.save(item1);

        Item item2 = new Item();
        item2.setName("Foo2");
        JOhm.save(item2);

        User user1 = new User();
        user1.setName("foo");
        user1.setThreeLatestPurchases(new Item[] { item0, item1, item2 });
        JOhm.save(user1);

        User user2 = new User();
        user2.setName("car");
        JOhm.save(user2);

        List<User> users = JOhm.find(User.class, "threeLatestPurchases", item0
                .getId());
        assertEquals(1, users.size());
        assertEquals(user1.getId(), users.get(0).getId());

        User user3 = new User();
        user3.setName("foo");
        user3.setThreeLatestPurchases(new Item[] { item0, item1, item2 });
        JOhm.save(user3);

        users = JOhm.find(User.class, "threeLatestPurchases", item0.getId());
        assertEquals(2, users.size());
        Long[] controlSet = { user1.getId(), user3.getId() };
        assertTrue(users.get(0).getId() != users.get(1).getId());
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(0).getId()) >= 0);
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(1).getId()) >= 0);
    }

    @Test
    public void canSearchOnSets() {
        Item item = new Item();
        item.setName("bar");
        JOhm.save(item);

        User user1 = new User();
        user1.setName("foo");
        JOhm.save(user1);
        user1.getPurchases().add(item);

        User user2 = new User();
        user2.setName("car");
        JOhm.save(user2);
        user2.getPurchases().add(item);

        List<User> users = JOhm.find(User.class, "purchases", item.getId());

        assertEquals(2, users.size());
        Long[] controlSet = { user1.getId(), user2.getId() };
        assertTrue(users.get(0).getId() != users.get(1).getId());
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(0).getId()) >= 0);
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(1).getId()) >= 0);
    }

    @Test
    public void canSearchOnSortedSets() {
        Item item = new Item();
        item.setName("bar");
        JOhm.save(item);

        User user1 = new User();
        user1.setName("foo");
        JOhm.save(user1);
        user1.getOrderedPurchases().add(item);

        User user2 = new User();
        user2.setName("car");
        JOhm.save(user2);
        user2.getOrderedPurchases().add(item);

        List<User> users = JOhm.find(User.class, "orderedPurchases", item
                .getId());

        assertEquals(2, users.size());
        Long[] controlSet = { user1.getId(), user2.getId() };
        assertTrue(users.get(0).getId() != users.get(1).getId());
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(0).getId()) >= 0);
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(1).getId()) >= 0);
    }

    @Test
    public void canSearchOnMaps() {
        Item item = new Item();
        item.setName("bar");
        JOhm.save(item);

        User user1 = new User();
        user1.setName("foo");
        JOhm.save(user1);
        user1.getFavoritePurchases().put(1, item);

        User user2 = new User();
        user2.setName("car");
        JOhm.save(user2);
        user2.getFavoritePurchases().put(1, item);

        List<User> users = JOhm.find(User.class, "favoritePurchases", item
                .getId());

        assertEquals(2, users.size());
        Long[] controlSet = { user1.getId(), user2.getId() };
        assertTrue(users.get(0).getId() != users.get(1).getId());
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(0).getId()) >= 0);
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(1).getId()) >= 0);
    }

    @Test
    public void canSearchOnReferences() {
        Country somewhere = new Country();
        somewhere.setName("somewhere");
        JOhm.save(somewhere);

        User user1 = new User();
        user1.setCountry(somewhere);
        JOhm.save(user1);

        User user2 = new User();
        user2.setCountry(somewhere);
        JOhm.save(user2);

        List<User> users = JOhm.find(User.class, "country", somewhere.getId());

        assertEquals(2, users.size());
        Long[] controlSet = { user1.getId(), user2.getId() };
        assertTrue(users.get(0).getId() != users.get(1).getId());
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(0).getId()) >= 0);
        assertTrue(Arrays.binarySearch(controlSet, 0, controlSet.length, users.get(1).getId()) >= 0);
    }

    @Test
    public void cannotSearchAfterDeletingIndexes() {
        User user = new User();
        user.setAge(88);
        JOhm.save(user);

        user.setAge(77); // younger
        JOhm.save(user);

        user.setAge(66); // younger still
        JOhm.save(user);

        Long id = user.getId();

        assertNotNull(JOhm.get(User.class, id));

        List<User> users = JOhm.find(User.class, "age", 88);
        assertEquals(0, users.size()); // index already updated
        users = JOhm.find(User.class, "age", 77);
        assertEquals(0, users.size()); // index already updated
        users = JOhm.find(User.class, "age", 66);
        assertEquals(1, users.size());

        JOhm.delete(User.class, id);

        users = JOhm.find(User.class, "age", 88);
        assertEquals(0, users.size());
        users = JOhm.find(User.class, "age", 77);
        assertEquals(0, users.size());
        users = JOhm.find(User.class, "age", 66);
        assertEquals(0, users.size());

        assertNull(JOhm.get(User.class, id));
    }
}