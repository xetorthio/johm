package redis.clients.johm.models;

import redis.clients.johm.Attribute;
import redis.clients.johm.Id;
import redis.clients.johm.Model;

@Model
public class TestMessage {
    @Id
    public Long id;
    @Attribute
    public String message;
}