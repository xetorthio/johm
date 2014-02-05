package redis.clients.johm.models;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.johm.Attribute;
import redis.clients.johm.Array;
import redis.clients.johm.CollectionList;
import redis.clients.johm.CollectionMap;
import redis.clients.johm.CollectionSet;
import redis.clients.johm.CollectionSortedSet;
import redis.clients.johm.Id;
import redis.clients.johm.Indexed;
import redis.clients.johm.Model;
import redis.clients.johm.Reference;

@Model
public class User {
    @Id
    private Long id;
    @Attribute
    @Indexed
    private String name;
    private String room;
    @Attribute
    @Indexed
    private int age;
    @Attribute
    private float salary;
    @Attribute
    private char initial;
    @Reference
    @Indexed
    private Country country;
    @CollectionList(of = Item.class)
    @Indexed
    private List<Item> likes;
    @CollectionSet(of = Item.class)
    @Indexed
    private Set<Item> purchases;
    @CollectionMap(key = Integer.class, value = Item.class)
    @Indexed
    private Map<Integer, Item> favoritePurchases;
    @CollectionSortedSet(of = Item.class, by = "price")
    @Indexed
    private Set<Item> orderedPurchases;
    @Array(of = Item.class, length = 3)
    @Indexed
    private Item[] threeLatestPurchases;

    public Long getId() {
        return id;
    }

    public List<Item> getLikes() {
        return likes;
    }

    public Set<Item> getPurchases() {
        return purchases;
    }

    public Set<Item> getOrderedPurchases() {
        return orderedPurchases;
    }

    public Map<Integer, Item> getFavoritePurchases() {
        return favoritePurchases;
    }

    public void setThreeLatestPurchases(Item[] threeLatestPurchases) {
        this.threeLatestPurchases = threeLatestPurchases;
    }

    public Item[] getThreeLatestPurchases() {
        return threeLatestPurchases;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public char getInitial() {
        return initial;
    }

    public void setInitial(char initial) {
        this.initial = initial;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + age;
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime
                * result
                + ((favoritePurchases == null) ? 0 : favoritePurchases
                        .hashCode());
        result = prime * result + initial;
        result = prime * result + ((likes == null) ? 0 : likes.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((purchases == null) ? 0 : purchases.hashCode());
        result = prime * result + ((room == null) ? 0 : room.hashCode());
        result = prime * result + Float.floatToIntBits(salary);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (age != other.age)
            return false;
        if (country == null) {
            if (other.country != null)
                return false;
        } else if (!country.equals(other.country))
            return false;
        if (favoritePurchases == null) {
            if (other.favoritePurchases != null)
                return false;
        } else if (!favoritePurchases.equals(other.favoritePurchases))
            return false;
        if (initial != other.initial)
            return false;
        if (likes == null) {
            if (other.likes != null)
                return false;
        } else if (!likes.equals(other.likes))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (purchases == null) {
            if (other.purchases != null)
                return false;
        } else if (!purchases.equals(other.purchases))
            return false;
        if (room == null) {
            if (other.room != null)
                return false;
        } else if (!room.equals(other.room))
            return false;
        if (Float.floatToIntBits(salary) != Float.floatToIntBits(other.salary))
            return false;
        return true;
    }
}
