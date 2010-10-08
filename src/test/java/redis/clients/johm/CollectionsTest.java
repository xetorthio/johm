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

    @Test(expected = MissingIdException.class)
    public void elementsInSetShouldBePersisted() {
        Item item = new Item();
        item.setName("Bar");

        User user = new User();
        user.save();
        user.getPurchases().add(item);
    }
}