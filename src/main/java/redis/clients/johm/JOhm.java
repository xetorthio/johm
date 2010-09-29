package redis.clients.johm;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import redis.clients.jedis.JedisPool;

public class JOhm {
    private static JedisPool jedisPool;
    private static Nest nest;

    public JOhm() {
    }

    public static void setPool(JedisPool jedisPool) {
	JOhm.jedisPool = jedisPool;
    }

    public static JedisPool getPool() {
	return jedisPool;
    }

    protected static Nest getNest() {
	if (nest == null) {
	    nest = new Nest(jedisPool);
	}
	return nest;
    }

    public static <T extends Model> T get(Class<? extends Model> clazz, int id) {
	Map<String, String> hashedObject = getNest().cat(clazz.getSimpleName())
		.cat(id).hgetAll();
	Model newInstance;
	Map<String, Field> fields = new HashMap<String, Field>();
	for (Field field : clazz.getDeclaredFields()) {
	    fields.put(field.getName(), field);
	}
	for (Field field : clazz.getSuperclass().getDeclaredFields()) {
	    fields.put(field.getName(), field);
	}

	try {
	    newInstance = clazz.newInstance();
	    Iterator<String> iterator = hashedObject.keySet().iterator();
	    while (iterator.hasNext()) {
		String fieldName = iterator.next();
		Field field = fields.get(fieldName);
		field.setAccessible(true);
		field.set(newInstance, convert(field, hashedObject
			.get(fieldName)));
	    }
	    return (T) newInstance;
	} catch (InstantiationException e) {
	    throw new JOhmException(e);
	} catch (IllegalAccessException e) {
	    throw new JOhmException(e);
	} catch (SecurityException e) {
	    throw new JOhmException(e);
	}
    }

    private static Object convert(Field field, String value) {
	if (field.getType().equals(Integer.class)) {
	    return new Integer(value);
	} else {
	    return value;
	}
    }
}
