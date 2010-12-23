package redis.clients.johm.models;

import java.util.Map;

import redis.clients.johm.Attribute;
import redis.clients.johm.CollectionMap;
import redis.clients.johm.Id;
import redis.clients.johm.Indexed;
import redis.clients.johm.Model;

@Model
public class Distribution {
    @Id
    private Integer id;
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

    public Integer getId() {
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

}