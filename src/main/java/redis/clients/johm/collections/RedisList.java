package redis.clients.johm.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import redis.clients.johm.JOhm;
import redis.clients.johm.Model;
import redis.clients.johm.Nest;

public class RedisList<T extends Model> implements java.util.List<T> {
    private Nest nest;
    private Class<? extends Model> clazz;
    private List<T> elements;

    public RedisList(Class<? extends Model> clazz, Nest nest) {
        this.clazz = clazz;
        this.nest = nest;
    }

    @SuppressWarnings("unchecked")
    private List<T> getElements() {
        if (elements == null) {
            elements = new ArrayList<T>();
            List<String> ids = nest.lrange(0, -1);
            for (String id : ids) {
                elements.add((T) JOhm.get(clazz, Integer.valueOf(id)));
            }
        }
        return elements;
    }

    public boolean add(T e) {
        return nest.rpush(e.getId().toString()) > 0;
    }

    public void add(int index, T element) {
        nest.lset(index, element.getId().toString());
    }

    public boolean addAll(Collection<? extends T> c) {
        for (T element : c) {
            nest.rpush(element.getId().toString());
        }
        return true;
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        for (T element : c) {
            nest.lset(index++, element.getId().toString());
        }
        return true;
    }

    public void clear() {
        nest.del();
    }

    public boolean contains(Object o) {
        return getElements().contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return getElements().containsAll(c);
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        String id = nest.lindex(index);
        return (T) JOhm.get(clazz, Integer.valueOf(id));
    }

    public int indexOf(Object o) {
        return getElements().indexOf(o);
    }

    public boolean isEmpty() {
        return nest.llen() == 0;
    }

    public Iterator<T> iterator() {
        return getElements().iterator();
    }

    public int lastIndexOf(Object o) {
        return getElements().lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return getElements().listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return getElements().listIterator(index);
    }

    public boolean remove(Object o) {
        Model element = (Model) o;
        Integer lrem = nest.lrem(1, element.getId().toString());
        return lrem > 0;
    }

    public T remove(int index) {
        T element = this.get(index);
        nest.lrem(1, element.getId().toString());
        return element;
    }

    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> c) {
        Iterator<? extends Model> iterator = (Iterator<? extends Model>) c
                .iterator();
        while (iterator.hasNext()) {
            T element = (T) iterator.next();
            this.remove(element);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean retainAll(Collection<?> c) {
        this.clear();
        Iterator<? extends Model> iterator = (Iterator<? extends Model>) c
                .iterator();
        while (iterator.hasNext()) {
            T element = (T) iterator.next();
            this.add(element);
        }
        return true;
    }

    public T set(int index, T element) {
        T previousElement = get(index);
        nest.lset(index, element.getId().toString());
        return previousElement;
    }

    public int size() {
        return nest.llen();
    }

    public java.util.List<T> subList(int fromIndex, int toIndex) {
        return getElements().subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return getElements().toArray();
    }

    @SuppressWarnings("hiding")
    public <T> T[] toArray(T[] a) {
        return getElements().toArray(a);
    }
}