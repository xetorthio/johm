package redis.clients.johm;

import java.util.Arrays;

import org.junit.Test;

import redis.clients.johm.models.Country;
import redis.clients.johm.models.Distribution;
import redis.clients.johm.models.Item;
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

    @Test
    public void testListDataTypeCombinations() {
        Distribution distro = new Distribution();
        distro.setDistroScope("World");
        JOhm.save(distro);

        Country country1 = new Country();
        country1.setName("FriendlyCountry");
        JOhm.save(country1);
        Country country2 = new Country();
        country2.setName("AngryCountry");
        JOhm.save(country2);

        distro.getCountriesOfWorld().add(country1);
        distro.getCountriesOfWorld().add(country2);

        Distribution savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(2, savedDistro.getCountriesOfWorld().size());

        assertEquals(country1, savedDistro.getCountriesOfWorld().get(0));
        assertEquals(country2, savedDistro.getCountriesOfWorld().get(1));

        savedDistro.getCountriesOfWorld().remove(0);
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(1, savedDistro.getCountriesOfWorld().size());
        assertEquals(country2, savedDistro.getCountriesOfWorld().get(0));

        savedDistro.getCountriesOfWorld().remove(0);
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(0, savedDistro.getCountriesOfWorld().size());

        distro.getCountrySizes().add(88888L);
        distro.getCountrySizes().add(99999L);
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(2, savedDistro.getCountrySizes().size());

        assertEquals(0, 88888L, savedDistro.getCountrySizes().get(0));
        assertEquals(0, 99999L, savedDistro.getCountrySizes().get(1));

        savedDistro.getCountrySizes().clear();
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(0, savedDistro.getCountrySizes().size());
    }

    @Test
    public void testSetDataTypeCombinations() {
        Distribution distro = new Distribution();
        distro.setDistroScope("World");
        JOhm.save(distro);

        Country country1 = new Country();
        country1.setName("United States");
        JOhm.save(country1);
        Country country2 = new Country();
        country2.setName("Canada");
        JOhm.save(country2);
        Country country3 = new Country();
        country3.setName("Mexico");
        JOhm.save(country3);

        distro.getNorthAmericanCountries().add(country1);
        distro.getNorthAmericanCountries().add(country2);
        distro.getNorthAmericanCountries().add(country3);

        Distribution savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(3, savedDistro.getNorthAmericanCountries().size());

        assertTrue(savedDistro.getNorthAmericanCountries().contains(country1));
        assertTrue(savedDistro.getNorthAmericanCountries().contains(country2));
        assertTrue(savedDistro.getNorthAmericanCountries().contains(country3));

        savedDistro.getNorthAmericanCountries().remove(country1);
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(2, savedDistro.getNorthAmericanCountries().size());
        assertTrue(savedDistro.getNorthAmericanCountries().contains(country2));
        assertTrue(savedDistro.getNorthAmericanCountries().contains(country3));

        savedDistro.getNorthAmericanCountries().clear();
        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(0, savedDistro.getNorthAmericanCountries().size());
    }

    @Test
    public void testArrayDataTypeCombinations() {
        Distribution distro = new Distribution();
        distro.setDistroScope("World");
        String[] planetNames = { "Mercury", "Venus", "Earth", "Mars",
                "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto" };
        distro.setPlanetNames(planetNames);
        JOhm.save(distro);

        Distribution savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(planetNames.length, savedDistro.getPlanetNames().length);
        assertTrue(Arrays.equals(savedDistro.getPlanetNames(), planetNames));

        Item ant = new Item();
        ant.setName("ant");
        ant.setPrice(0f);
        ant = JOhm.save(ant);

        Item mvn = new Item();
        mvn.setName("maven");
        mvn.setPrice(0f);
        mvn = JOhm.save(mvn);

        Item sbt = new Item();
        sbt.setName("sbt");
        sbt.setPrice(0f);
        sbt = JOhm.save(sbt);

        Item rake = new Item();
        rake.setName("rake");
        rake.setPrice(0f);
        rake = JOhm.save(rake);

        Item make = new Item();
        make.setName("make");
        make.setPrice(0f);
        make = JOhm.save(make);

        Item[] buildTools = { ant, mvn, sbt, rake, make };
        distro.setBuildTools(buildTools);
        JOhm.save(distro);

        savedDistro = JOhm.get(Distribution.class, distro.getId());
        assertEquals(buildTools.length, savedDistro.getBuildTools().length);
        assertTrue(Arrays.asList(savedDistro.getBuildTools()).contains(ant));
        assertTrue(Arrays.asList(savedDistro.getBuildTools()).contains(mvn));
        assertTrue(Arrays.asList(savedDistro.getBuildTools()).contains(sbt));
        assertTrue(Arrays.asList(savedDistro.getBuildTools()).contains(rake));
        assertTrue(Arrays.asList(savedDistro.getBuildTools()).contains(make));
    }
}