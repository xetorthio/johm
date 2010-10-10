package redis.clients.johm.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
public class RedisSet<T extends Model> extends RedisBaseCollection implements
        Set<T> {
    private final Nest nest;
    private final Class<? extends Model> clazz;
    private final Set<T> elements;

    public RedisSet(final Class<? extends Model> clazz, final Nest nest) {
        this.clazz = clazz;
        this.nest = nest;
        elements = new LinkedHashSet<T>();
    }

    @Override
    public int size() {
        int repoSize = nest.smembers().size();
        if (repoSize != elements.size()) {
            refreshStorage(true);
        }
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
        return internalAdd(element, true);
    }

    @Override
    public boolean remove(Object o) {
        return internalRemove(o, true);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return scrollElements().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean success = true;
        for (T element : collection) {
            success &= internalAdd(element, false);
        }
        refreshStorage(true);
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
            success &= internalAdd(element, false);
        }
        refreshStorage(true);
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
            success &= internalRemove(element, false);
        }
        refreshStorage(true);
        return success;
    }

    @Override
    public void clear() {
        nest.del();
        elements.clear();
    }

    private boolean internalAdd(T element, boolean refreshStorage) {
        element.save();
        boolean success = nest.sadd(element.getId().toString()) > 0;
        if (refreshStorage) { // don't trust success-value too much
            refreshStorage(true);
        }
        return success;
    }

    private boolean internalRemove(Object o, boolean refreshStorage) {
        Model element = Model.class.cast(o);
        boolean success = nest.srem(element.getId().toString()) > 0;
        element.delete();
        if (refreshStorage) { // don't trust success-value too much
            // Since we cannot guarantee all Model's will provide a reasonable
            // equals() and hashCode() implementation, using remove() on the Set
            // cannot guarantee container-storage purge.
            refreshStorage(true);
        }
        return success;
    }

    protected synchronized void purgeScrollStorage() {
        elements.clear();
        scrollElements();
    }

    @SuppressWarnings("unchecked")
    private synchronized Set<T> scrollElements() {
        if (elements.isEmpty()) {
            Set<String> ids = nest.smembers();
            for (String id : ids) {
                elements.add((T) JOhm.get(clazz, Integer.valueOf(id)));
            }
        }
        return elements;
    }
}