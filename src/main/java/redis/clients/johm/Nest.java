package redis.clients.johm;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.TransactionBlock;

/**
 * 
 */
public class Nest {
    private static final String COLON = ":";
    private StringBuilder sb;
    private String key;
    private static JedisPool jedisPool;

    public static void setJedisPool(JedisPool jedisPool) {
        Nest.jedisPool = jedisPool;
    }

    public Nest() {
        this.key = "";
    }

    public Nest(String key) {
        this.key = key;
    }

    public Nest(Class<? extends Model> clazz) {
        this.key = clazz.getSimpleName();
    }

    public Nest(JOhm jOhm) {
        this.key = jOhm.getClass().getSimpleName();
    }

    public String key() {
        prefix();
        String generatedKey = sb.toString();
        generatedKey = generatedKey.substring(0, generatedKey.length() - 1);
        sb = null;
        return generatedKey;
    }

    private void prefix() {
        if (sb == null) {
            sb = new StringBuilder();
            sb.append(key);
            sb.append(COLON);
        }
    }

    public Nest cat(int id) {
        prefix();
        sb.append(id);
        sb.append(COLON);
        return this;
    }

    public Nest cat(String field) {
        prefix();
        sb.append(field);
        sb.append(COLON);
        return this;
    }

    public String set(String value) {
        Jedis jedis = getResource();
        String set = jedis.set(key(), value);
        jedisPool.returnResource(jedis);
        return set;
    }

    public String get() {
        Jedis jedis = getResource();
        String string = jedis.get(key());
        jedisPool.returnResource(jedis);
        return string;
    }

    public Integer incr() {
        Jedis jedis = getResource();
        Integer incr = jedis.incr(key());
        jedisPool.returnResource(jedis);
        return incr;
    }

    public String hmset(Map<String, String> hash) {
        Jedis jedis = getResource();
        String hmset = jedis.hmset(key(), hash);
        jedisPool.returnResource(jedis);
        return hmset;
    }

    public List<Object> multi(TransactionBlock transaction) {
        Jedis jedis = getResource();
        List<Object> multi = jedis.multi(transaction);
        jedisPool.returnResource(jedis);
        return multi;
    }

    public Integer del() {
        Jedis jedis = getResource();
        Integer del = jedis.del(key());
        jedisPool.returnResource(jedis);
        return del;
    }

    public Map<String, String> hgetAll() {
        Jedis jedis = getResource();
        Map<String, String> hgetAll = jedis.hgetAll(key());
        jedisPool.returnResource(jedis);
        return hgetAll;
    }

    public Integer exists() {
        Jedis jedis = getResource();
        Integer exists = jedis.exists(key());
        jedisPool.returnResource(jedis);
        return exists;
    }

    public Integer sadd(String member) {
        Jedis jedis = getResource();
        Integer reply = jedis.sadd(key(), member);
        jedisPool.returnResource(jedis);
        return reply;
    }
    
    public Integer srem(String member) {
        Jedis jedis = getResource();
        Integer reply = jedis.srem(key(), member);
        jedisPool.returnResource(jedis);
        return reply;
    }

    public Set<String> smembers() {
        Jedis jedis = getResource();
        Set<String> members = jedis.smembers(key());
        jedisPool.returnResource(jedis);
        return members;
    }

    private static Jedis getResource() {
        Jedis jedis;
        try {
            jedis = jedisPool.getResource();
        } catch (TimeoutException e) {
            throw new JOhmException(e);
        }
        return jedis;
    }

    public Nest cat(Object field) {
        prefix();
        sb.append(field);
        sb.append(COLON);
        return this;
    }

    public Integer rpush(String string) {
        Jedis jedis = getResource();
        Integer rpush = jedis.rpush(key(), string);
        jedisPool.returnResource(jedis);
        return rpush;
    }

    public String lset(int index, String value) {
        Jedis jedis = getResource();
        String lset = jedis.lset(key(), index, value);
        jedisPool.returnResource(jedis);
        return lset;
    }

    public String lindex(int index) {
        Jedis jedis = getResource();
        String lindex = jedis.lindex(key(), index);
        jedisPool.returnResource(jedis);
        return lindex;
    }

    public Integer llen() {
        Jedis jedis = getResource();
        Integer llen = jedis.llen(key());
        jedisPool.returnResource(jedis);
        return llen;
    }

    public Integer lrem(int count, String value) {
        Jedis jedis = getResource();
        Integer lrem = jedis.lrem(key(), count, value);
        jedisPool.returnResource(jedis);
        return lrem;
    }

    public List<String> lrange(int start, int end) {
        Jedis jedis = getResource();
        List<String> lrange = jedis.lrange(key(), start, end);
        jedisPool.returnResource(jedis);
        return lrange;
    }

    public Nest fork() {
        return new Nest(key());
    }
}