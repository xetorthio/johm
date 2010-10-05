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
final class Nest {
    private static final String COLON = ":";
    private StringBuilder sb;
    private String key;
    private static JedisPool jedisPool;

    Nest(JedisPool jedis) {
        this.key = "";
        Nest.jedisPool = jedis;
    }

    Nest(String key, JedisPool jedis) {
        this.key = key;
        Nest.jedisPool = jedis;
    }

    String key() {
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

    Nest cat(int id) {
        prefix();
        sb.append(id);
        sb.append(COLON);
        return this;
    }

    Nest cat(String field) {
        prefix();
        sb.append(field);
        sb.append(COLON);
        return this;
    }

    String set(String value) {
        Jedis jedis = getResource();
        String set = jedis.set(key(), value);
        jedisPool.returnResource(jedis);
        return set;
    }

    String get() {
        Jedis jedis = getResource();
        String string = jedis.get(key());
        jedisPool.returnResource(jedis);
        return string;
    }

    Integer incr() {
        Jedis jedis = getResource();
        Integer incr = jedis.incr(key());
        jedisPool.returnResource(jedis);
        return incr;
    }

    String hmset(Map<String, String> hash) {
        Jedis jedis = getResource();
        String hmset = jedis.hmset(key(), hash);
        jedisPool.returnResource(jedis);
        return hmset;
    }

    List<Object> multi(TransactionBlock transaction) {
        Jedis jedis = getResource();
        List<Object> multi = jedis.multi(transaction);
        jedisPool.returnResource(jedis);
        return multi;
    }

    Integer del() {
        Jedis jedis = getResource();
        Integer del = jedis.del(key());
        jedisPool.returnResource(jedis);
        return del;
    }

    Map<String, String> hgetAll() {
        Jedis jedis = getResource();
        Map<String, String> hgetAll = jedis.hgetAll(key());
        jedisPool.returnResource(jedis);
        return hgetAll;
    }

    Integer exists() {
        Jedis jedis = getResource();
        Integer exists = jedis.exists(key());
        jedisPool.returnResource(jedis);
        return exists;
    }

    static Integer sadd(String key, String member) {
        Jedis jedis = getResource();
        Integer reply = jedis.sadd(key, member);
        jedisPool.returnResource(jedis);
        return reply;
    }

    static Integer sismember(String key, String member) {
        Jedis jedis = getResource();
        Integer reply = jedis.sismember(key, member);
        jedisPool.returnResource(jedis);
        return reply;
    }

    static Set<String> smembers(String key) {
        Jedis jedis = getResource();
        Set<String> members = jedis.smembers(key);
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

}