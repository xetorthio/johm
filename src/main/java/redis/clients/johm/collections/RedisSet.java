package redis.clients.johm.collections;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import redis.clients.johm.Indexed;
import redis.clients.johm.JOhm;
import redis.clients.johm.JOhmUtils;
import redis.clients.johm.Nest;

/**
 * RedisSet is a JOhm-internal Set implementation to serve as a proxy for the
 * Redis persisted set and provide lazy-loading semantics to minimize datastore
 * network traffic. It does a best-effort job of minimizing set entity staleness
 * but does so without any locking and is not thread-safe. It also maintains
 * whatever order in which Redis returns its set elements. Only add and remove
 * trigger a remote-sync of local internal storage.
 */
public class RedisSet<T> implements Set<T> {
    private final Nest<? extends T> nest;
    private final Class<? extends T> clazz;
    private final Object owner;
    private final Field field;

    public RedisSet(final Class<? extends T> clazz,
            final Nest<? extends T> nest, Field field, Object owner) {
        this.clazz = clazz;
        this.nest = nest;
        this.field = field;
        this.owner = owner;
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

    @Override
    public int size() {
        int repoSize = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .smembers().size();
        return repoSize;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return scrollElements().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return scrollElements().iterator();
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

    @Override
    public boolean add(T element) {
        return internalAdd(element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        return internalRemove((T) o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return scrollElements().containsAll(c);
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

    @Override
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

    @Override
    public void clear() {
        nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).del();
    }

    private boolean internalAdd(T element) {
        boolean success = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .sadd(JOhmUtils.getId(element).toString()) > 0;
        indexValue(element);
        return success;
    }

    private boolean internalRemove(T element) {
        boolean success = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .srem(JOhmUtils.getId(element).toString()) > 0;
        unindexValue(element);
        return success;
    }

    @SuppressWarnings("unchecked")
    private synchronized Set<T> scrollElements() {
        Set<String> ids = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .smembers();
        Set<T> elements = new HashSet<T>();
        for (String id : ids) {
            elements.add((T) JOhm.get(clazz, Integer.valueOf(id)));
        }
        return elements;
    }
}