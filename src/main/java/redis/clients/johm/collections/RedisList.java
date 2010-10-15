package redis.clients.johm.collections;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import redis.clients.johm.Indexed;
import redis.clients.johm.JOhm;
import redis.clients.johm.JOhmUtils;
import redis.clients.johm.Model;
import redis.clients.johm.Nest;

/**
 * RedisList is a JOhm-internal List implementation to serve as a proxy for the
 * Redis persisted list and provide lazy-loading semantics to minimize datastore
 * network traffic. It does a best-effort job of minimizing list entity
 * staleness but does so without any locking and is not thread-safe. Only add
 * and remove operations trigger a remote-sync of local internal storage.
 */
public class RedisList<T extends Model> implements java.util.List<T> {
    private Nest nest;
    private Class<? extends Model> clazz;
    private Field field;
    private Model owner;

    public RedisList(Class<? extends Model> clazz, Nest nest, Field field,
            Model owner) {
        this.clazz = clazz;
        this.nest = nest;
        this.field = field;
        this.owner = owner;
    }

    @Override
    public boolean add(T e) {
        return internalAdd(e);
    }

    @Override
    public void add(int index, T element) {
        internalIndexedAdd(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean success = true;
        for (T element : c) {
            success &= internalAdd(element);
        }
        return success;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        for (T element : c) {
            internalIndexedAdd(index++, element);
        }
        return true;
    }

    @Override
    public void clear() {
        nest.cat(owner.getId()).cat(field.getName()).del();
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
    public T get(int index) {
        T element = null;
        String id = nest.cat(owner.getId()).cat(field.getName()).lindex(index);
        if (!JOhmUtils.isNullOrEmpty(id)) {
            element = JOhm.get(clazz, Integer.valueOf(id));
        }
        return element;
    }

    @Override
    public int indexOf(Object o) {
        return scrollElements().indexOf(o);
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
    public int lastIndexOf(Object o) {
        return scrollElements().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return scrollElements().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return scrollElements().listIterator(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        return internalRemove((T) o);
    }

    @Override
    public T remove(int index) {
        return internalIndexedRemove(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean success = true;
        Iterator<? extends Model> iterator = (Iterator<? extends Model>) c
                .iterator();
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
    public T set(int index, T element) {
        T previousElement = this.get(index);
        internalIndexedAdd(index, element);
        return previousElement;
    }

    @Override
    public int size() {
        int repoSize = nest.cat(owner.getId()).cat(field.getName()).llen();
        return repoSize;
    }

    @Override
    public java.util.List<T> subList(int fromIndex, int toIndex) {
        return scrollElements().subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return scrollElements().toArray();
    }

    @SuppressWarnings("hiding")
    @Override
    public <T> T[] toArray(T[] a) {
        return scrollElements().toArray(a);
    }

    private boolean internalAdd(T element) {
        boolean success = nest.cat(owner.getId()).cat(field.getName()).rpush(
                element.getId().toString()) > 0;
        indexValue(element);
        return success;
    }

    private void indexValue(T element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            nest.cat(field.getName()).cat(element.getId()).sadd(
                    owner.getId().toString());
        }
    }

    private void unindexValue(T element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            nest.cat(field.getName()).cat(element.getId()).srem(
                    owner.getId().toString());
        }
    }

    private void internalIndexedAdd(int index, T element) {
        nest.cat(owner.getId()).cat(field.getName()).lset(index,
                element.getId().toString());
        indexValue(element);
    }

    private boolean internalRemove(T element) {
        Integer lrem = nest.cat(owner.getId()).cat(field.getName()).lrem(1,
                element.getId().toString());
        unindexValue(element);
        return lrem > 0;
    }

    private T internalIndexedRemove(int index) {
        T element = this.get(index);
        internalRemove(element);
        return element;
    }

    @SuppressWarnings("unchecked")
    private synchronized List<T> scrollElements() {
        List<T> elements = new ArrayList<T>();

        List<String> ids = nest.cat(owner.getId()).cat(field.getName()).lrange(
                0, -1);
        for (String id : ids) {
            elements.add((T) JOhm.get(clazz, Integer.valueOf(id)));
        }
        return elements;
    }
}