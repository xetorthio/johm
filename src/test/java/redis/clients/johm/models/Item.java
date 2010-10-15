package redis.clients.johm.models;

import redis.clients.johm.Attribute;
import redis.clients.johm.Model;

public class Item extends Model {
    @Attribute
    private String name;
    @Attribute
    private Float price;

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
