package redis.clients.johm.collections;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import redis.clients.johm.JOhm;
import redis.clients.johm.JOhmException;
import redis.clients.johm.Model;
import redis.clients.johm.Nest;

/**
 * RedisSortedSet is a JOhm-internal SortedSet implementation to serve as a
 * proxy for the Redis persisted sorted set.
 */
public class RedisSortedSet<T extends Model> implements Set<T> {
    private final Nest nest;
    private final Class<? extends Model> clazz;
    private final String field;

    public RedisSortedSet(final Class<? extends Model> clazz, String field,
            final Nest nest) {
        this.clazz = clazz;
        this.nest = nest;
        this.field = field;
    }

    @SuppressWarnings("unchecked")
    private synchronized Set<T> scrollElements() {
        Set<String> ids = nest.zrange(0, -1);
        Set<T> elements = new LinkedHashSet<T>();
        for (String id : ids) {
            elements.add((T) JOhm.get(clazz, Integer.valueOf(id)));
        }
        return elements;
    }

    private boolean internalAdd(T element) {
        Field declaredField;
        boolean success = false;
        try {
            declaredField = element.getClass().getDeclaredField(this.field);
            declaredField.setAccessible(true);
            Object fieldValue = declaredField.get(element);
            success = nest.zadd(Float.class.cast(fieldValue), element.getId()
                    .toString()) > 0;

        } catch (SecurityException e) {
            throw new JOhmException(e);
        } catch (NoSuchFieldException e) {
            throw new JOhmException(e);
        } catch (IllegalArgumentException e) {
            throw new JOhmException(e);
        } catch (IllegalAccessException e) {
            throw new JOhmException(e);
        }
        return success;
    }

    private boolean internalRemove(Object o) {
        Model element = Model.class.cast(o);
        boolean success = nest.srem(element.getId().toString()) > 0;
        return success;
    }

    @Override
    public boolean add(T e) {
        return internalAdd(e);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean success = true;
        for (T element : collection) {
            success &= internalAdd(element);
        }
        return success;
    }

    @Override
    public void clear() {
        nest.del();
    }

    @Override
    public boolean contains(Object o) {
        return scrollElements().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return scrollElements().containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return scrollElements().iterator();
    }

    @Override
    public boolean remove(Object o) {
        return internalRemove(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(Collection<?> c) {
        Iterator<? extends Model> iterator = (Iterator<? extends Model>) c
                .iterator();
        boolean success = true;
        while (iterator.hasNext()) {
            T element = (T) iterator.next();
            success &= internalRemove(element);
        }
        return success;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(Collection<?> c) {
        this.clear();
        Iterator<? extends Model> iterator = (Iterator<? extends Model>) c
                .iterator();
        boolean success = true;
        while (iterator.hasNext()) {
            T element = (T) iterator.next();
            success &= internalAdd(element);
        }
        return success;

    }

    @Override
    public int size() {
        int repoSize = nest.zcard();
        return repoSize;
    }

    @Override
    public Object[] toArray() {
        return scrollElements().toArray();
    }

    @Override
    @SuppressWarnings("hiding")
    public <T> T[] toArray(T[] a) {
        return scrollElements().toArray(a);
    }
}