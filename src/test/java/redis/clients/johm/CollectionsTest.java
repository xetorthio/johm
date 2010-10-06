package redis.clients.johm;

import org.junit.Test;

import redis.clients.johm.collections.RedisList;
import redis.clients.johm.models.Item;
import redis.clients.johm.models.User;

public class CollectionsTest extends JOhmTestBase {
    @Test
    public void shouldSetCollectionAutomatically() {
        User user = new User();
        assertNotNull(user.getLikes());
        assertTrue(user.getLikes().getClass().equals(RedisList.class));
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

}