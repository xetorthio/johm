package redis.clients.johm.collections;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import redis.clients.johm.Indexed;
import redis.clients.johm.JOhm;
import redis.clients.johm.JOhmUtils;
import redis.clients.johm.JOhmUtils.JOhmCollectionDataType;
import redis.clients.johm.Nest;

/**
 * RedisMap is a JOhm-internal Map implementation to serve as a proxy for the
 * Redis persisted hash and provide lazy-loading semantics to minimize datastore
 * network traffic. It does a best-effort job of minimizing hash staleness but
 * does so without any locking and is not thread-safe. Only add and remove
 * operations trigger a remote-sync of local internal storage.
 * 
 * RedisMap does not support null keys or values.
 */
public class RedisMap<K, V> implements Map<K, V> {
    private final Nest<? extends V> nest;
    private final Class<? extends K> keyClazz;
    private final Class<? extends V> valueClazz;
    private final JOhmCollectionDataType johmKeyType;
    private final JOhmCollectionDataType johmValueType;
    private final Field field;
    private final Object owner;

    public RedisMap(final Class<? extends K> keyClazz,
            final Class<? extends V> valueClazz, final Nest<? extends V> nest,
            Field field, Object owner) {
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
        johmKeyType = JOhmUtils.detectJOhmCollectionDataType(keyClazz);
        johmValueType = JOhmUtils.detectJOhmCollectionDataType(valueClazz);
        this.nest = nest;
        this.field = field;
        this.owner = owner;
    }

    private void indexValue(K element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            if (johmKeyType == JOhmCollectionDataType.PRIMITIVE) {
                nest.cat(field.getName()).cat(element).sadd(
                        JOhmUtils.getId(owner).toString());
            } else if (johmKeyType == JOhmCollectionDataType.MODEL) {
                nest.cat(field.getName()).cat(JOhmUtils.getId(element)).sadd(
                        JOhmUtils.getId(owner).toString());
            }
        }
    }

    private void unindexValue(K element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            if (johmKeyType == JOhmCollectionDataType.PRIMITIVE) {
                nest.cat(field.getName()).cat(element).srem(
                        JOhmUtils.getId(owner).toString());
            } else if (johmKeyType == JOhmCollectionDataType.MODEL) {
                nest.cat(field.getName()).cat(JOhmUtils.getId(element)).srem(
                        JOhmUtils.getId(owner).toString());
            }
        }
    }

    public void clear() {
        Map<String, String> savedHash = nest.cat(JOhmUtils.getId(owner)).cat(
                field.getName()).hgetAll();
        for (Map.Entry<String, String> entry : savedHash.entrySet()) {
            nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).hdel(
                    entry.getKey());
        }
        nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).del();
    }

    public boolean containsKey(Object key) {
        return scrollElements().containsKey(key);
    }

    public boolean containsValue(Object value) {
        return scrollElements().containsValue(value);
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return scrollElements().entrySet();
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        V value = null;
        String valueKey = null;
        if (johmKeyType == JOhmCollectionDataType.PRIMITIVE) {
            valueKey = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                    .hget(key.toString());
        } else if (johmKeyType == JOhmCollectionDataType.MODEL) {
            valueKey = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                    .hget(JOhmUtils.getId(key).toString());
        }

        if (!JOhmUtils.isNullOrEmpty(valueKey)) {
            if (johmValueType == JOhmCollectionDataType.PRIMITIVE) {
                value = (V) JOhmUtils.converter.getAsObject(valueClazz, valueKey);
            } else if (johmValueType == JOhmCollectionDataType.MODEL) {
                value = JOhm.<V> get(valueClazz, Integer.parseInt(valueKey));
            }
        }
        return value;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    @SuppressWarnings("unchecked")
    public Set<K> keySet() {
        Set<K> keys = new LinkedHashSet<K>();
        for (String key : nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .hkeys()) {
            if (johmKeyType == JOhmCollectionDataType.PRIMITIVE) {
                keys.add((K) JOhmUtils.converter.getAsObject(keyClazz, key));
            } else if (johmKeyType == JOhmCollectionDataType.MODEL) {
                keys.add(JOhm.<K> get(keyClazz, Integer.parseInt(key)));
            }
        }
        return keys;
    }

    public V put(K key, V value) {
        V previousValue = get(key);
        internalPut(key, value);
        return previousValue;
    }

    public void putAll(Map<? extends K, ? extends V> mapToCopyIn) {
        for (Map.Entry<? extends K, ? extends V> entry : mapToCopyIn.entrySet()) {
            internalPut(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        V value = get(key);
        if (johmKeyType == JOhmCollectionDataType.PRIMITIVE) {
            nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).hdel(
                    key.toString());
        } else if (johmKeyType == JOhmCollectionDataType.MODEL) {
            nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).hdel(
                    JOhmUtils.getId(key).toString());
        }
        unindexValue((K) key);
        return value;
    }

    public int size() {
        return nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).hlen()
                .intValue();
    }

    public Collection<V> values() {
        return scrollElements().values();
    }

    private V internalPut(final K key, final V value) {
        Map<String, String> hash = new LinkedHashMap<String, String>();
        String keyString = null;
        String valueString = null;
        if (johmKeyType == JOhmCollectionDataType.PRIMITIVE
                && johmValueType == JOhmCollectionDataType.PRIMITIVE) {
            keyString = key.toString();
            valueString = value.toString();
        } else if (johmKeyType == JOhmCollectionDataType.PRIMITIVE
                && johmValueType == JOhmCollectionDataType.MODEL) {
            keyString = key.toString();
            valueString = JOhmUtils.getId(value).toString();
        } else if (johmKeyType == JOhmCollectionDataType.MODEL
                && johmValueType == JOhmCollectionDataType.PRIMITIVE) {
            keyString = JOhmUtils.getId(key).toString();
            valueString = value.toString();
        } else if (johmKeyType == JOhmCollectionDataType.MODEL
                && johmValueType == JOhmCollectionDataType.MODEL) {
            keyString = JOhmUtils.getId(key).toString();
            valueString = JOhmUtils.getId(value).toString();
        }

        hash.put(keyString, valueString);
        nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).hmset(hash);
        indexValue(key);
        return value;
    }

    @SuppressWarnings("unchecked")
    private synchronized Map<K, V> scrollElements() {
        Map<String, String> savedHash = nest.cat(JOhmUtils.getId(owner)).cat(
                field.getName()).hgetAll();
        Map<K, V> backingMap = new HashMap<K, V>();
        K savedKey = null;
        V savedValue = null;
        for (Map.Entry<String, String> entry : savedHash.entrySet()) {
            if (johmKeyType == JOhmCollectionDataType.PRIMITIVE
                    && johmValueType == JOhmCollectionDataType.PRIMITIVE) {
                savedKey = (K) JOhmUtils.converter.getAsObject(keyClazz, entry
                        .getKey());
                savedValue = (V) JOhmUtils.converter.getAsObject(valueClazz, entry
                        .getValue());
            } else if (johmKeyType == JOhmCollectionDataType.PRIMITIVE
                    && johmValueType == JOhmCollectionDataType.MODEL) {
                savedKey = (K) JOhmUtils.converter.getAsObject(keyClazz, entry
                        .getKey());
                savedValue = JOhm.<V> get(valueClazz, Integer.parseInt(entry
                        .getValue()));
            } else if (johmKeyType == JOhmCollectionDataType.MODEL
                    && johmValueType == JOhmCollectionDataType.PRIMITIVE) {
                savedKey = JOhm.<K> get(keyClazz, Integer.parseInt(entry
                        .getKey()));
                savedValue = (V) JOhmUtils.converter.getAsObject(valueClazz, entry
                        .getValue());
            } else if (johmKeyType == JOhmCollectionDataType.MODEL
                    && johmValueType == JOhmCollectionDataType.MODEL) {
                savedKey = JOhm.<K> get(keyClazz, Integer.parseInt(entry
                        .getKey()));
                savedValue = JOhm.<V> get(valueClazz, Integer.parseInt(entry
                        .getValue()));
            }

            backingMap.put(savedKey, savedValue);
        }

        return backingMap;
    }
}
