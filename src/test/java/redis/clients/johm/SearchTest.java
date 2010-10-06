package redis.clients.johm;

import java.util.List;

import org.junit.Test;

import redis.clients.johm.models.User;

public class SearchTest extends JOhmTestBase {
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

}