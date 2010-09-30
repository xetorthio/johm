package redis.clients.johm.models;

import redis.clients.johm.Attribute;
import redis.clients.johm.Model;

public class User extends Model {
    @Attribute
    private String name;
    private String room;
    @Attribute
    private int age;
    @Attribute
    private float salary;
    @Attribute
    private char initial;

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
}
