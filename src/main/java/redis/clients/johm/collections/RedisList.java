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
import redis.clients.johm.JOhmUtils.JOhmCollectionDataType;
import redis.clients.johm.Nest;

/**
 * RedisList is a JOhm-internal List implementation to serve as a proxy for the
 * Redis persisted list and provide lazy-loading semantics to minimize datastore
 * network traffic. It does a best-effort job of minimizing list entity
 * staleness but does so without any locking and is not thread-safe. Only add
 * and remove operations trigger a remote-sync of local internal storage.
 * 
 * RedisList does not support null elements.
 */
public class RedisList<T> implements java.util.List<T> {
    private final Nest<? extends T> nest;
    private final Class<? extends T> elementClazz;
    private final JOhmCollectionDataType johmElementType;
    private final Field field;
    private final Object owner;

    public RedisList(Class<? extends T> clazz, Nest<? extends T> nest,
            Field field, Object owner) {
        this.elementClazz = clazz;
        johmElementType = JOhmUtils.detectJOhmCollectionDataType(clazz);
        this.nest = nest;
        this.field = field;
        this.owner = owner;
    }

    public boolean add(T e) {
        return internalAdd(e);
    }

    public void add(int index, T element) {
        internalIndexedAdd(index, element);
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean success = true;
        for (T element : c) {
            success &= internalAdd(element);
        }
        return success;
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        for (T element : c) {
            internalIndexedAdd(index++, element);
        }
        return true;
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

    @SuppressWarnings("unchecked")
    public T get(int index) {
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

    public int indexOf(Object o) {
        return scrollElements().indexOf(o);
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public Iterator<T> iterator() {
        return scrollElements().iterator();
    }

    public int lastIndexOf(Object o) {
        return scrollElements().lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return scrollElements().listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return scrollElements().listIterator(index);
    }

    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        return internalRemove((T) o);
    }

    public T remove(int index) {
        return internalIndexedRemove(index);
    }

    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> c) {
        boolean success = true;
        Iterator<?> iterator = (Iterator<?>) c.iterator();
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

    public T set(int index, T element) {
        T previousElement = this.get(index);
        internalIndexedAdd(index, element);
        return previousElement;
    }

    public int size() {
        return nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).llen()
                .intValue();
    }

    public java.util.List<T> subList(int fromIndex, int toIndex) {
        return scrollElements().subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return scrollElements().toArray();
    }

    @SuppressWarnings("hiding")
    public <T> T[] toArray(T[] a) {
        return scrollElements().toArray(a);
    }

    private boolean internalAdd(T element) {
        boolean success = false;
        if (element != null) {
            if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
                success = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                        .rpush(element.toString()) > 0;
            } else if (johmElementType == JOhmCollectionDataType.MODEL) {
                success = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                        .rpush(JOhmUtils.getId(element).toString()) > 0;
            }
            indexValue(element);
        }
        return success;
    }

    private void indexValue(T element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
                nest.cat(field.getName()).cat(element.toString()).sadd(
                        JOhmUtils.getId(owner).toString());
            } else if (johmElementType == JOhmCollectionDataType.MODEL) {
                nest.cat(field.getName()).cat(JOhmUtils.getId(element)).sadd(
                        JOhmUtils.getId(owner).toString());
            }
        }
    }

    private void unindexValue(T element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
                nest.cat(field.getName()).cat(element.toString()).srem(
                        JOhmUtils.getId(owner).toString());
            } else if (johmElementType == JOhmCollectionDataType.MODEL) {
                nest.cat(field.getName()).cat(JOhmUtils.getId(element)).srem(
                        JOhmUtils.getId(owner).toString());
            }
        }
    }

    private void internalIndexedAdd(int index, T element) {
        if (element != null) {
            if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
                nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).lset(
                        index, element.toString());
            } else if (johmElementType == JOhmCollectionDataType.MODEL) {
                nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).lset(
                        index, JOhmUtils.getId(element).toString());
            }
            indexValue(element);
        }
    }

    private boolean internalRemove(T element) {
        boolean success = false;
        if (element != null) {
            Long lrem = 0L;
            if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
                lrem = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                        .lrem(1, element.toString());
            } else if (johmElementType == JOhmCollectionDataType.MODEL) {
                lrem = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                        .lrem(1, JOhmUtils.getId(element).toString());
            }
            unindexValue(element);
            success = lrem > 0L;
        }
        return success;
    }

    private T internalIndexedRemove(int index) {
        T element = this.get(index);
        internalRemove(element);
        return element;
    }

    @SuppressWarnings("unchecked")
    private synchronized List<T> scrollElements() {
        List<T> elements = new ArrayList<T>();

        List<String> keys = nest.cat(JOhmUtils.getId(owner)).cat(
                field.getName()).lrange(0, -1);
        for (String key : keys) {
            if (johmElementType == JOhmCollectionDataType.PRIMITIVE) {
                elements.add((T) JOhmUtils.converter.getAsObject(elementClazz, key));
            } else if (johmElementType == JOhmCollectionDataType.MODEL) {
                elements.add((T) JOhm.get(elementClazz, Integer.valueOf(key)));
            }
        }
        return elements;
    }
}