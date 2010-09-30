package redis.clients.johm;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
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

	    if (newInstance.nest.cat(id).exists().intValue() == 0) {
		return null;
	    }
	    Map<String, String> hashedObject = newInstance.nest.cat(id)
		    .hgetAll();

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
	Class<?> type = field.getType();
	if (type.equals(Byte.class) || type.equals(byte.class)) {
	    return new Byte(value);
	}
	if (type.equals(Character.class) || type.equals(char.class)) {
	    if (value != null && value.trim().length() > 0) {
		return value.charAt(0);
	    } else {
		// This is the default value
		return '\u0000';
	    }
	}
	if (type.equals(Short.class) || type.equals(short.class)) {
	    return new Short(value);
	}
	if (type.equals(Integer.class) || type.equals(int.class)) {
	    if (value == null) {
		return 0;
	    }
	    return new Integer(value);
	}
	if (type.equals(Float.class) || type.equals(float.class)) {
	    if (value == null) {
		return 0L;
	    }
	    return new Float(value);
	}
	if (type.equals(Double.class) || type.equals(double.class)) {
	    return new Double(value);
	}
	if (type.equals(Long.class) || type.equals(long.class)) {
	    return new Long(value);
	}

	// Higher precision folks
	if (type.equals(BigDecimal.class)) {
	    return new BigDecimal(value);
	}
	if (type.equals(BigInteger.class)) {
	    return new BigInteger(value);
	}

	if (type.isEnum()) {
	    // return Enum.valueOf(type, value);
	    return null; // TODO: handle these
	}

	// Collections not yet supported
	if (type.equals(Collection.class)) {
	    return null; // TODO: handle these
	}
	if (type.isArray()) {
	    return null; // TODO: handle these
	}

	return value;
    }

    protected static void checkValidReference(Field field) {
	if (!field.getType().getClass().isInstance(Model.class)) {
	    throw new JOhmException(field.getType().getSimpleName()
		    + " is not a subclass of Model");
	}
    }

    public static boolean delete(Class<? extends Model> clazz, int id) {
	boolean deleted = false;
	Model persistedModel = get(clazz, id);
	if (persistedModel != null) {
	    deleted = persistedModel.nest.cat(id).del() == 1;
	}
	return deleted;
    }
}