package redis.clients.johm.collections;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import redis.clients.johm.Indexed;
import redis.clients.johm.JOhm;
import redis.clients.johm.JOhmException;
import redis.clients.johm.JOhmExceptionMeta;
import redis.clients.johm.JOhmUtils;
import redis.clients.johm.Nest;

/**
 * RedisSortedSet is a JOhm-internal SortedSet implementation to serve as a
 * proxy for the Redis persisted sorted set.
 */
public class RedisSortedSet<T> implements Set<T> {
    private final Nest<? extends T> nest;
    private final Class<? extends T> clazz;
    private final Field field;
    private final Object owner;
    private final String byFieldName;

    public RedisSortedSet(Class<? extends T> clazz, String byField,
            Nest<? extends T> nest, Field field, Object owner) {
        this.clazz = clazz;
        this.nest = nest;
        this.field = field;
        this.owner = owner;
        this.byFieldName = byField;
    }

    @SuppressWarnings("unchecked")
    private synchronized Set<T> scrollElements() {
        Set<String> ids = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .zrange(0, -1);
        Set<T> elements = new LinkedHashSet<T>();
        for (String id : ids) {
            elements.add((T) JOhm.get(clazz, Integer.valueOf(id)));
        }
        return elements;
    }

    private void indexValue(T element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            nest.cat(field.getName()).cat(JOhmUtils.getId(element)).sadd(
                    JOhmUtils.getId(owner).toString());
        }
    }

    private void unindexValue(T element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            nest.cat(field.getName()).cat(JOhmUtils.getId(element)).srem(
                    JOhmUtils.getId(owner).toString());
        }
    }

    private boolean internalAdd(T element) {
        boolean success = false;
        if (element != null) {
            try {
                Field byField = element.getClass()
                        .getDeclaredField(byFieldName);
                byField.setAccessible(true);
                Object fieldValue = byField.get(element);
                if (fieldValue == null) {
                    fieldValue = 0f;
                }
                success = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                        .zadd(Float.class.cast(fieldValue),
                                JOhmUtils.getId(element).toString()) > 0;
                indexValue(element);
            } catch (SecurityException e) {
                throw new JOhmException(e, JOhmExceptionMeta.SECURITY_EXCEPTION);
            } catch (IllegalArgumentException e) {
                throw new JOhmException(e,
                        JOhmExceptionMeta.ILLEGAL_ARGUMENT_EXCEPTION);
            } catch (IllegalAccessException e) {
                throw new JOhmException(e,
                        JOhmExceptionMeta.ILLEGAL_ACCESS_EXCEPTION);
            } catch (NoSuchFieldException e) {
                throw new JOhmException(e,
                        JOhmExceptionMeta.NO_SUCH_FIELD_EXCEPTION);
            }
        }
        return success;
    }

    private boolean internalRemove(T element) {
        boolean success = false;
        if (element != null) {
            success = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                    .srem(JOhmUtils.getId(element).toString()) > 0;
            unindexValue(element);
        }
        return success;
    }

    public boolean add(T e) {
        return internalAdd(e);
    }

    public boolean addAll(Collection<? extends T> collection) {
        boolean success = true;
        for (T element : collection) {
            success &= internalAdd(element);
        }
        return success;
    }

    public void clear() {
        nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).del();
    }

    public boolean contains(Object o) {
        return scrollElements().contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return scrollElements().containsAll(c);
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public Iterator<T> iterator() {
        return scrollElements().iterator();
    }

    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        return internalRemove((T) o);
    }

    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> c) {
        Iterator<?> iterator = (Iterator<?>) c.iterator();
        boolean success = true;
        while (iterator.hasNext()) {
            T element = (T) iterator.next();
            success &= internalRemove(element);
        }
        return success;
    }

    @SuppressWarnings("unchecked")
    public boolean retainAll(Collection<?> c) {
        this.clear();
        Iterator<?> iterator = (Iterator<?>) c.iterator();
        boolean success = true;
        while (iterator.hasNext()) {
            T element = (T) iterator.next();
            success &= internalAdd(element);
        }
        return success;

    }

    public int size() {
        return nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).zcard()
                .intValue();
    }

    public Object[] toArray() {
        return scrollElements().toArray();
    }

    @SuppressWarnings("hiding")
    public <T> T[] toArray(T[] a) {
        return scrollElements().toArray(a);
    }
}