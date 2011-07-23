package redis.clients.johm;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class Convertor {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US);

    static {
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
    }

    static Object string2object(final Field field, final String value) {
        return string2object(field.getType(), value);
    }

    public static String object2string(final Field field, final Object value) {
        return object2string(field.getType(), value);
    }

    public static Object string2object(final Class<?> type, final String value) {
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
            try {
                return sdf.parse(value);
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }

        if (type.isEnum() || type.equals(Enum.class)) {
            // return Enum.valueOf(type, value);
            return null; // TODO: handle these
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

    public static String object2string(final Class<?> type, final Object value) {
        if (Date.class.equals(type)) {
            return sdf.format((Date)value);
        }
        return value.toString();
    }
}
