package redis.clients.johm;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
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
        Map<String, Field> fields = new HashMap<String, Field>();
        for (Field field : clazz.getDeclaredFields()) {
            fields.put(field.getName(), field);
        }
        for (Field field : clazz.getSuperclass().getDeclaredFields()) {
            fields.put(field.getName(), field);
        }
        try {
            Model newInstance = clazz.newInstance();
            Map<String, String> hashedObject = getNest().cat(clazz.getSimpleName())
                .cat(id).hgetAll();
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
    
    public static boolean delete(Class<? extends Model> clazz, int id) {
        boolean deleted = false;
        Model persistedModel = get(clazz, id);
        if (persistedModel != null) {
            deleted = getNest().cat(clazz.getSimpleName()).cat(id).del() == 1;
        }
        return deleted;
    }

    private static Object convert(Field field, String value) {
        Class<?> type = field.getType();
        if (type.equals(Byte.class) || type.equals(byte.class)) {
            return new Byte(value);
        }
        if (type.equals(Character.class) || type.equals(char.class)) {
            if (value != null && value.trim().length() > 0) {
                return value.charAt(0);
            }
        }
        if (type.equals(Short.class) || type.equals(short.class)) {
            return new Short(value);
        }
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return new Integer(value);
        }
        if (type.equals(Float.class) || type.equals(float.class)) {
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
}
