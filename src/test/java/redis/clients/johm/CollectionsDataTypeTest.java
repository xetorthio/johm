package redis.clients.johm;

import org.junit.Test;

import redis.clients.johm.models.Country;
import redis.clients.johm.models.Distribution;
import redis.clients.johm.models.User;

public class CollectionsDataTypeTest extends JOhmTestBase {
    @Test
    public void testMapDataTypeCombinations() {
        Distribution distro = new Distribution();
        distro.setDistroScope("World");
        JOhm.save(distro);

        // K = Primitive, V = Primitive
        distro.getAgeNameDistribution().put(10, "John");
        distro.getAgeNameDistribution().put(35, "Doe");

        Distribution savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(2, savedDistro.getAgeNameDistribution().size());

        assertEquals("John", savedDistro.getAgeNameDistribution().get(10));
        assertEquals("Doe", savedDistro.getAgeNameDistribution().get(35));

        distro.getAgeNameDistribution().remove(10);
        distro.getAgeNameDistribution().remove(35);
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(0, savedDistro.getAgeNameDistribution().size());

        // K = Model, V = Primitive
        Country country1 = new Country();
        country1.setName("FriendlyCountry");
        JOhm.save(country1);
        Country country2 = new Country();
        country2.setName("AngryCountry");
        JOhm.save(country2);

        distro.getCountryAverageAgeDistribution().put(country1, 30);
        distro.getCountryAverageAgeDistribution().put(country2, 90);
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(2, savedDistro.getCountryAverageAgeDistribution().size());

        assertTrue(30 == savedDistro.getCountryAverageAgeDistribution().get(
                country1));
        assertTrue(90 == savedDistro.getCountryAverageAgeDistribution().get(
                country2));

        distro.getCountryAverageAgeDistribution().remove(country1);
        distro.getCountryAverageAgeDistribution().remove(country2);
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(0, savedDistro.getCountryAverageAgeDistribution().size());

        // K = Primitive, V = Model
        distro.getNameCountryDistribution().put("John", country1);
        distro.getNameCountryDistribution().put("Doe", country2);

        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(2, savedDistro.getNameCountryDistribution().size());

        assertEquals(country1, savedDistro.getNameCountryDistribution().get(
                "John"));
        assertEquals(country2, savedDistro.getNameCountryDistribution().get(
                "Doe"));

        distro.getNameCountryDistribution().remove("John");
        distro.getNameCountryDistribution().remove("Doe");
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(0, savedDistro.getNameCountryDistribution().size());

        // K = Model, V = Model
        User user1 = new User();
        user1.setName("Happy");
        JOhm.save(user1);
        User user2 = new User();
        user2.setName("Frightened");
        JOhm.save(user2);

        distro.getUserCitizenshipDistribution().put(user1, country1);
        distro.getUserCitizenshipDistribution().put(user2, country2);

        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(2, savedDistro.getUserCitizenshipDistribution().size());

        assertEquals(country1, savedDistro.getUserCitizenshipDistribution()
                .get(user1));
        assertEquals(country2, savedDistro.getUserCitizenshipDistribution()
                .get(user2));

        savedDistro.getUserCitizenshipDistribution().remove(user1);
        savedDistro.getUserCitizenshipDistribution().remove(user2);
        assertEquals(0, savedDistro.getUserCitizenshipDistribution().size());
    }

}