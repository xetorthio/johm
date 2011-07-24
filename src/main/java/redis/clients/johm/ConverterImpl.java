package redis.clients.johm;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ConverterImpl implements Converter {

    private final Set<Class<?>> JOHM_SUPPORTED_PRIMITIVES = new HashSet<Class<?>>();

    public ConverterImpl() {
        JOHM_SUPPORTED_PRIMITIVES.add(String.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Byte.class);
        JOHM_SUPPORTED_PRIMITIVES.add(byte.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Character.class);
        JOHM_SUPPORTED_PRIMITIVES.add(char.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Short.class);
        JOHM_SUPPORTED_PRIMITIVES.add(short.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Integer.class);
        JOHM_SUPPORTED_PRIMITIVES.add(int.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Float.class);
        JOHM_SUPPORTED_PRIMITIVES.add(float.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Double.class);
        JOHM_SUPPORTED_PRIMITIVES.add(double.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Long.class);
        JOHM_SUPPORTED_PRIMITIVES.add(long.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Boolean.class);
        JOHM_SUPPORTED_PRIMITIVES.add(boolean.class);
        JOHM_SUPPORTED_PRIMITIVES.add(BigDecimal.class);
        JOHM_SUPPORTED_PRIMITIVES.add(BigInteger.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Date.class);
        JOHM_SUPPORTED_PRIMITIVES.add(Enum.class);
    }

    public Object getAsObject(Field field, String value) {
        return getAsObject(field.getType(), value);
    }

    public String getAsString(Field field, Object value) {
        return getAsString(field.getType(), value);
    }

    public boolean isSupportedPrimitive(Class<?> cls) {
        if (cls.isEnum()) {
            return true;
        }
        return JOHM_SUPPORTED_PRIMITIVES.contains(cls); 
    }

    public Object getAsObject(Class<?> type, String value) {
        if (type.equals(Byte.class) || type.equals(byte.class)) {
            return new Byte(value);
        }
        if (type.equals(Character.class) || type.equals(char.class)) {
            if (!JOhmUtils.isNullOrEmpty(value)) {
                if (value.length() > 1) {
                    throw new IllegalArgumentException(
                            "Non-character value masquerading as characters in a string");
                }
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
                return 0f;
            }
            return new Float(value);
        }
        if (type.equals(Double.class) || type.equals(double.class)) {
            return new Double(value);
        }
        if (type.equals(Long.class) || type.equals(long.class)) {
            return new Long(value);
        }
        if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return new Boolean(value);
        }

        // Higher precision folks
        if (type.equals(BigDecimal.class)) {
            return new BigDecimal(value);
        }
        if (type.equals(BigInteger.class)) {
            return new BigInteger(value);
        }

        if (type.equals(Date.class)) {
            if (value == null) {
                return null;
            }
            return new Date(Long.parseLong(value));
        }

        if (value != null && (type.isEnum() || type.equals(Enum.class))) {
            return Enum.valueOf((Class<? extends Enum>)type, value);
        }

        // Raw Collections are unsupported
        if (type.equals(Collection.class)) {
            return null;
        }

        // Raw arrays are unsupported
        if (type.isArray()) {
            return null;
        }

        return value;
    }

    public String getAsString(Class<?> type, Object value) {
        if (Date.class.equals(type)) {
            return "" + ((Date)value).getTime();
        }
        return value.toString();
    }
}
