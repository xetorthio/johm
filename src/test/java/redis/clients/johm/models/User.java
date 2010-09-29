package redis.clients.johm.models;

import redis.clients.johm.Attribute;
import redis.clients.johm.Model;

public class User extends Model {
    @Attribute
    private String name;
    private String room;

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
}
