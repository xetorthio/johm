package redis.clients.johm.collections;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import redis.clients.johm.Indexed;
import redis.clients.johm.JOhm;
import redis.clients.johm.JOhmUtils;
import redis.clients.johm.JOhmUtils.JOhmCollectionDataType;
import redis.clients.johm.Nest;

/**
 * RedisArray is a JOhm-internal 1-Dimensional Array implementation to serve as
 * a proxy for the Redis persisted array. The backing Redis persistence model is
 * that of a list.
 * 
 * This is a special case of JOhm collections and is treated different from
 * others primarily due to the non-growable nature of the static-array data
 * structure. As a consequence, we will not prevent JOhm users from doing their
 * own array allocation using the 'new' keyword. This mode of usage makes the
 * RedisArray somewhat of a bridge case between say an Attribute and a dynamic
 * collection like RedisList.
 * 
 * RedisArray does not support null data elements.
 */
public class RedisArray<T> {
    private final int length;
    private final Class<? extends T> elementClazz;
    private final JOhmCollectionDataType johmElementType;
    private final Nest<? extends T> nest;
    private final Field field;
    private final Object owner;
    private boolean isIndexed;

    public RedisArray(int length, Class<? extends T> clazz,
            Nest<? extends T> nest, Field field, Object owner) {
        this.length = length;
        this.elementClazz = clazz;
        johmElementType = JOhmUtils.detectJOhmCollectionDataType(clazz);
        this.nest = nest;
        this.field = field;
        isIndexed = field.isAnnotationPresent(Indexed.class);
        this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    public T[] read() {
        T[] streamed = (T[]) Array.newInstance(elementClazz, length);
        for (int iter = 0; iter < length; iter++) {
            streamed[iter] = elementClazz.cast(get(iter));
        }
        return streamed;
    }

    public void write(T[] backingArray) {
        if (backingArray == null || backingArray.length == 0) {
            clear();
        } else {
            for (int iter = 0; iter < backingArray.length; iter++) {
                delete(iter);
                save(backingArray[iter]);
            }
        }
    }

    public Long clear() {
        return nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).del();
    }

    private boolean save(T element) {
        boolean success = false;
        if (element != null) {
            if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
                success = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                        .rpush(element.toString()) > 0;
            } else if (johmElementType == JOhmCollectionDataType.MODEL) {
                success = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                        .rpush(JOhmUtils.getId(element).toString()) > 0;
            }
            if (isIndexed) {
                indexValue(element);
            }
        }
        return success;
    }

    private void delete(int index) {
        T element = this.get(index);
        internalDelete(element);
    }

    @SuppressWarnings("unchecked")
    private T get(int index) {
        T element = null;
        String key = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .lindex(index);
        if (!JOhmUtils.isNullOrEmpty(key)) {
            if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
                element = (T) JOhmUtils.converter.getAsObject(elementClazz, key);
            } else if (johmElementType == JOhmCollectionDataType.MODEL) {
                element = JOhm.<T> get(elementClazz, Integer.valueOf(key));
            }
        }
        return element;
    }

    private void indexValue(T element) {
        if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
            nest.cat(field.getName()).cat(element.toString()).sadd(
                    JOhmUtils.getId(owner).toString());
        } else if (johmElementType == JOhmCollectionDataType.MODEL) {
            nest.cat(field.getName()).cat(JOhmUtils.getId(element)).sadd(
                    JOhmUtils.getId(owner).toString());
        }
    }

    private void unindexValue(T element) {
        if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
            nest.cat(field.getName()).cat(element.toString()).srem(
                    JOhmUtils.getId(owner).toString());
        } else if (johmElementType == JOhmCollectionDataType.MODEL) {
            nest.cat(field.getName()).cat(JOhmUtils.getId(element)).srem(
                    JOhmUtils.getId(owner).toString());
        }
    }

    private boolean internalDelete(T element) {
        if (element == null) {
            return false;
        }
        Long lrem = 0L;
        if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
            lrem = nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).lrem(
                    1, element.toString());
        } else if (johmElementType == JOhmCollectionDataType.MODEL) {
            lrem = nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).lrem(
                    1, JOhmUtils.getId(element).toString());
        }
        if (isIndexed) {
            unindexValue(element);
        }
        return lrem > 0L;
    }
}