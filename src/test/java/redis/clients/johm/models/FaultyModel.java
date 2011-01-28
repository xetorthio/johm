package redis.clients.johm.models;

import redis.clients.johm.Array;
import redis.clients.johm.Attribute;
import redis.clients.johm.Id;
import redis.clients.johm.Model;

@Model
public class FaultyModel {
    @Id
    @Array(of = long.class, length = 1)
    private long id;
    @Attribute
    private String type;

    public long getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
