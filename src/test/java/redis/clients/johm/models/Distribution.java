package redis.clients.johm.models;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.johm.Array;
import redis.clients.johm.Attribute;
import redis.clients.johm.CollectionList;
import redis.clients.johm.CollectionMap;
import redis.clients.johm.CollectionSet;
import redis.clients.johm.Id;
import redis.clients.johm.Indexed;
import redis.clients.johm.Model;

@Model
public class Distribution {
    @Id
    private Long id;
    @Attribute
    private String distroScope;
    @CollectionMap(key = Integer.class, value = String.class)
    @Indexed
    private Map<Integer, String> ageNameDistribution;
    @CollectionMap(key = Country.class, value = Integer.class)
    @Indexed
    private Map<Country, Integer> countryAverageAgeDistribution;
    @CollectionMap(key = String.class, value = Country.class)
    @Indexed
    private Map<String, Country> nameCountryDistribution;
    @CollectionMap(key = User.class, value = Country.class)
    @Indexed
    private Map<User, Country> userCitizenshipDistribution;
    @CollectionList(of = Country.class)
    @Indexed
    private List<Country> countriesOfWorld;
    @CollectionList(of = Long.class)
    @Indexed
    private List<Long> countrySizes;
    @CollectionSet(of = Country.class)
    @Indexed
    private Set<Country> northAmericanCountries;
    @Array(of = String.class, length = 9)
    @Indexed
    private String[] planetNames;
    @Array(of = Item.class, length = 5)
    @Indexed
    private Item[] buildTools;

    public Long getId() {
        return id;
    }

    public String getDistroScope() {
        return distroScope;
    }

    public void setDistroScope(String distroScope) {
        this.distroScope = distroScope;
    }

    public Map<Integer, String> getAgeNameDistribution() {
        return ageNameDistribution;
    }

    public Map<Country, Integer> getCountryAverageAgeDistribution() {
        return countryAverageAgeDistribution;
    }

    public Map<String, Country> getNameCountryDistribution() {
        return nameCountryDistribution;
    }

    public Map<User, Country> getUserCitizenshipDistribution() {
        return userCitizenshipDistribution;
    }

    public List<Country> getCountriesOfWorld() {
        return countriesOfWorld;
    }

    public List<Long> getCountrySizes() {
        return countrySizes;
    }

    public Set<Country> getNorthAmericanCountries() {
        return northAmericanCountries;
    }

    public String[] getPlanetNames() {
        return planetNames;
    }

    public void setPlanetNames(String[] planetNames) {
        this.planetNames = planetNames;
    }

    public Item[] getBuildTools() {
        return buildTools;
    }

    public void setBuildTools(Item[] buildTools) {
        this.buildTools = buildTools;
    }
}