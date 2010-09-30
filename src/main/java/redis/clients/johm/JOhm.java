package redis.clients.johm;

import java.lang.reflect.Field;
import java.util.Map;

import redis.clients.jedis.JedisPool;

public class JOhm {
    private static JedisPool jedisPool;
    protected Nest nest = null;

    public JOhm() {
	nest = new Nest(this.getClass().getSimpleName(), jedisPool);
    }

    public static void setPool(JedisPool jedisPool) {
	JOhm.jedisPool = jedisPool;
    }

    public static JedisPool getPool() {
	return jedisPool;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Model> T get(Class<? extends Model> clazz, int id) {
	Model newInstance;
	try {
	    newInstance = clazz.newInstance();

	    Map<String, String> hashedObject = newInstance.nest.cat(id)
		    .hgetAll();

	    System.out.println(hashedObject);
	    for (Field field : clazz.getDeclaredFields()) {
		fillField(hashedObject, newInstance, field);
	    }
	    for (Field field : clazz.getSuperclass().getDeclaredFields()) {
		fillField(hashedObject, newInstance, field);
	    }

	    return (T) newInstance;
	} catch (InstantiationException e) {
	    throw new JOhmException(e);
	} catch (IllegalAccessException e) {
	    throw new JOhmException(e);
	}
    }

    @SuppressWarnings("unchecked")
    private static void fillField(Map<String, String> hashedObject,
	    Model newInstance, Field field) throws IllegalAccessException {
	if (field.isAnnotationPresent(Attribute.class)) {
	    field.setAccessible(true);
	    field.set(newInstance, convert(field, hashedObject.get(field
		    .getName())));
	}
	if (field.isAnnotationPresent(Reference.class)) {
	    checkValidReference(field);
	    field.setAccessible(true);
	    String serializedReferenceId = hashedObject
		    .get(getReferenceFieldName(field));
	    if (serializedReferenceId != null) {
		Integer referenceId = Integer.valueOf(serializedReferenceId);
		field.set(newInstance, get((Class<? extends Model>) field
			.getType(), referenceId));
	    }
	}
    }

    protected static String getReferenceFieldName(Field field) {
	return field.getName() + "_id";
    }

    private static Object convert(Field field, String value) {
	if (field.getType().equals(Integer.class)) {
	    return new Integer(value);
	} else {
	    return value;
	}
    }

    protected static void checkValidReference(Field field) {
	if (!field.getType().getClass().isInstance(Model.class)) {
	    throw new JOhmException(field.getType().getSimpleName()
		    + " is not a subclass of Model");
	}
    }

}
