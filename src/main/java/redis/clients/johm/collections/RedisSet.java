package redis.clients.johm.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import redis.clients.johm.JOhm;
import redis.clients.johm.Model;
import redis.clients.johm.Nest;

/**
 * RedisSet is a JOhm-internal Set implementation to serve as a proxy for the
 * Redis persisted set and provide lazy-loading semantics to minimize datastore
 * network traffic. It does a best-effort job of minimizing set entity staleness
 * but does so without any locking and is not thread-safe. It also maintains
 * whatever order in which Redis returns its set elements. Only add and remove
 * trigger a remote-sync of local internal storage.
 */
public class RedisSet<T extends Model> implements Set<T> {
    private final Nest nest;
    private final Class<? extends Model> clazz;

    public RedisSet(final Class<? extends Model> clazz, final Nest nest) {
        this.clazz = clazz;
        this.nest = nest;
    }

    @Override
    public int size() {
        int repoSize = nest.smembers().size();
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

    @Override
    public boolean remove(Object o) {
        return internalRemove(o);
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
    @SuppressWarnings("unchecked")
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

    @Override
    public void clear() {
        nest.del();
    }

    private boolean internalAdd(T element) {
        boolean success = nest.sadd(element.getId().toString()) > 0;
        return success;
    }

    private boolean internalRemove(Object o) {
        Model element = Model.class.cast(o);
        boolean success = nest.srem(element.getId().toString()) > 0;
        return success;
    }

    @SuppressWarnings("unchecked")
    private synchronized Set<T> scrollElements() {
        Set<String> ids = nest.smembers();
        Set<T> elements = new HashSet<T>();
        for (String id : ids) {
            elements.add((T) JOhm.get(clazz, Integer.valueOf(id)));
        }
        return elements;
    }
}