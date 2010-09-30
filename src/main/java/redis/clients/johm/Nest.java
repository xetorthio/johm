package redis.clients.johm;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.TransactionBlock;

public class Nest {
    private static final String COLON = ":";
    private StringBuilder sb;
    private String key;
    private JedisPool jedisPool;

    public Nest(JedisPool jedis) {
	this.key = "";
	this.jedisPool = jedis;
    }

    public Nest(String key, JedisPool jedis) {
	this.key = key;
	this.jedisPool = jedis;
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
	Jedis jedis;
	try {
	    jedis = jedisPool.getResource();
	} catch (TimeoutException e) {
	    throw new JOhmException(e);
	}
	String set = jedis.set(key(), value);
	jedisPool.returnResource(jedis);
	return set;
    }

    public String get() {
	Jedis jedis;
	try {
	    jedis = jedisPool.getResource();
	} catch (TimeoutException e) {
	    throw new JOhmException(e);
	}
	String string = jedis.get(key());
	jedisPool.returnResource(jedis);
	return string;
    }

    public Integer incr() {
	Jedis jedis;
	try {
	    jedis = jedisPool.getResource();
	} catch (TimeoutException e) {
	    throw new JOhmException(e);
	}
	Integer incr = jedis.incr(key());
	jedisPool.returnResource(jedis);

	return incr;
    }

    public String hmset(Map<String, String> hash) {
	Jedis jedis;
	try {
	    jedis = jedisPool.getResource();
	} catch (TimeoutException e) {
	    throw new JOhmException(e);
	}
	String hmset = jedis.hmset(key(), hash);
	jedisPool.returnResource(jedis);
	return hmset;
    }

    public List<Object> multi(TransactionBlock transaction) {
	Jedis jedis;
	try {
	    jedis = jedisPool.getResource();
	} catch (TimeoutException e) {
	    throw new JOhmException(e);
	}
	List<Object> multi = jedis.multi(transaction);
	jedisPool.returnResource(jedis);
	return multi;
    }

    public Integer del() {
	Jedis jedis;
	try {
	    jedis = jedisPool.getResource();
	} catch (TimeoutException e) {
	    throw new JOhmException(e);
	}
	Integer del = jedis.del(key());
	jedisPool.returnResource(jedis);
	return del;
    }

    public Map<String, String> hgetAll() {
	Jedis jedis;
	try {
	    jedis = jedisPool.getResource();
	} catch (TimeoutException e) {
	    throw new JOhmException(e);
	}
	Map<String, String> hgetAll = jedis.hgetAll(key());
	jedisPool.returnResource(jedis);
	return hgetAll;
    }
}