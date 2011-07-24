package redis.clients.johm;

import java.lang.reflect.Field;

public interface Converter {
    Object getAsObject(Field field, String value);
    Object getAsObject(Class<?> type, String value);
    String getAsString(Field field, Object value);
    String getAsString(Class<?> type, Object value);
    boolean isSupportedPrimitive(Class<?> cls);
}
