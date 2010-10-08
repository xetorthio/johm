package redis.clients.johm;

import org.junit.Test;

import redis.clients.johm.collections.RedisList;
import redis.clients.johm.collections.RedisSet;
import redis.clients.johm.models.Item;
import redis.clients.johm.models.User;

public class CollectionsTest extends JOhmTestBase {
    @Test
    public void shouldSetCollectionAutomatically() {
        User user = new User();
        assertNotNull(user.getLikes());
        assertTrue(user.getLikes().getClass().equals(RedisList.class));
        assertTrue(user.getPurchases().getClass().equals(RedisSet.class));
    }

    @Test
    public void persistList() {
        Item item = new Item();
        item.setName("Foo");
        item.save();

        User user = new User();
        user.save();
        user.getLikes().add(item);

        User savedUser = JOhm.get(User.class, user.getId());

        assertEquals(1, savedUser.getLikes().size());

        Item savedItem = savedUser.getLikes().get(0);

        assertNotNull(savedItem);
        assertEquals(item.getId(), savedItem.getId());
        assertEquals(item.getName(), savedItem.getName());
    }

    @Test
    public void persistListAndCheckModifications() {
        Item item1 = new Item();
        item1.setName("Foo");
        item1.save();

        User user = new User();
        user.save();
        user.getLikes().add(item1);

        User savedUser = JOhm.get(User.class, user.getId());

        assertEquals(1, savedUser.getLikes().size());

        Item savedItem1 = savedUser.getLikes().get(0);
        assertNotNull(savedItem1);
        assertEquals(item1.getId(), savedItem1.getId());
        assertEquals(item1.getName(), savedItem1.getName());

        Item item2 = new Item();
        item2.setName("Bar");
        item2.save();
        user.getLikes().add(item2);

        assertEquals(2, savedUser.getLikes().size());

        Item savedItem2 = savedUser.getLikes().get(1);
        assertNotNull(savedItem2);
        assertEquals(item2.getId(), savedItem2.getId());
        assertEquals(item2.getName(), savedItem2.getName());

        user.getLikes().clear();
        assertEquals(0, savedUser.getLikes().size());

        user.getLikes().add(item2);
        assertEquals(1, savedUser.getLikes().size());
        savedItem2 = savedUser.getLikes().get(0);
        assertNotNull(savedItem2);
        assertEquals(item2.getId(), savedItem2.getId());
        assertEquals(item2.getName(), savedItem2.getName());
    }

    @Test(expected = MissingIdException.class)
    public void elementsInListShouldBePersisted() {
        Item item = new Item();
        item.setName("Foo");

        User user = new User();
        user.save();
        user.getLikes().add(item);
    }

    @Test
    public void persistSet() {
        Item item = new Item();
        item.setName("Bar");
        item.save();

        User user = new User();
        user.save();
        user.getPurchases().add(item);

        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(1, savedUser.getPurchases().size());

        Item savedItem = savedUser.getPurchases().iterator().next();
        assertNotNull(savedItem);
        assertEquals(item.getId(), savedItem.getId());
        assertEquals(item.getName(), savedItem.getName());
    }

    @Test
    public void persistSetAndCheckModifications() {
        Item item1 = new Item();
        item1.setName("Bar");
        item1.save();

        User user = new User();
        user.save();
        user.getPurchases().add(item1);

        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(1, savedUser.getPurchases().size());

        Item savedItem1 = savedUser.getPurchases().iterator().next();
        assertNotNull(savedItem1);
        assertEquals(item1.getId(), savedItem1.getId());
        assertEquals(item1.getName(), savedItem1.getName());

        Item item2 = new Item();
        item2.setName("Bar");
        item2.save();
        user.getPurchases().add(item2);

        assertEquals(2, savedUser.getPurchases().size());

        user.getPurchases().clear();
        assertEquals(0, savedUser.getPurchases().size());

        user.getPurchases().add(item2);
        assertEquals(1, savedUser.getPurchases().size());
        Item savedItem2 = savedUser.getPurchases().iterator().next();
        assertNotNull(savedItem2);
        assertEquals(item2.getId(), savedItem2.getId());
        assertEquals(item2.getName(), savedItem2.getName());
    }

    @Test(expected = MissingIdException.class)
    public void elementsInSetShouldBePersisted() {
        Item item = new Item();
        item.setName("Bar");

        User user = new User();
        user.save();
        user.getPurchases().add(item);
    }
}