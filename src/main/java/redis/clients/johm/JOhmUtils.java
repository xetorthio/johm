package redis.clients.johm;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

final class JOhmUtils {
    static Object convert(final Field field, final String value) {
        Class<?> type = field.getType();
        if (type.equals(Byte.class) || type.equals(byte.class)) {
            return new Byte(value);
        }
        if (type.equals(Character.class) || type.equals(char.class)) {
            if (!isNullOrEmpty(value)) {
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

    static void checkValidReference(final Field field) {
        if (!field.getType().getClass().isInstance(Model.class)) {
            throw new JOhmException(field.getType().getSimpleName()
                    + " is not a subclass of Model");
        }
    }

    static String getReferenceFieldName(final Field field) {
        return field.getName() + "_id";
    }

    static String createSearchKey(final String attributeName,
            final Object attributeValue) {
        String key = null;
        // Prevent null/empty keys and null/empty values but allow
        // whitespace as a set value
        if (attributeName != null && attributeName.trim().length() > 0) {
            if (attributeValue != null) {
                String value = attributeValue.toString();
                if (!isNullOrEmpty(value)) {
                    key = attributeName.trim() + ":" + value;
                }
            }
        }
        return key;
    }

    static boolean isNullOrEmpty(String string) {
        boolean isNullOrEmpty = false;
        if (string == null || string.trim().length() == 0) {
            isNullOrEmpty = true;
        }

        return isNullOrEmpty;
    }
}
