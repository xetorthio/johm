package redis.clients.johm.models;

import redis.clients.johm.*;

import java.util.List;

@Model
public class Test {
    @Id
    public Long id;
    @Attribute
    public String name;
    @CollectionList(of = TestMessage.class)
    public List<TestMessage> messages;
}