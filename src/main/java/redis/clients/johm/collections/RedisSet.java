package redis.clients.johm.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import redis.clients.johm.JOhm;
import redis.clients.johm.Model;
import redis.clients.johm.Nest;

public class RedisSet<T extends Model> implements Set<T> {
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
        return nest.smembers().size();
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
        return nest.sadd(element.getId().toString()) > 0;
    }

    @Override
    public boolean remove(Object o) {
        // Since we cannot guarantee all Model's will provide a reasonable
        // equals() and hashCode() implementation, using remove() on the Set
        // cannot guarantee container-storage purge.
        if (!elements.isEmpty()) {
            elements.clear();
        }
        Model element = Model.class.cast(o);
        return nest.srem(element.getId().toString()) > 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return scrollElements().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean success = true;
        for (T element : collection) {
            success &= this.add(element);
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
            success &= this.add(element);
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
            success &= this.remove(element);
        }
        return success;
    }

    @Override
    public void clear() {
        nest.del();
    }

    @SuppressWarnings("unchecked")
    private Set<T> scrollElements() {
        if (elements.isEmpty()) {
            Set<String> ids = nest.smembers();
            for (String id : ids) {
                elements.add((T) JOhm.get(clazz, Integer.valueOf(id)));
            }
        }
        return elements;
    }
}
