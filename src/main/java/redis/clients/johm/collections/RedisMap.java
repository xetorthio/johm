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
import redis.clients.johm.Nest;

/**
 * RedisMap is a JOhm-internal Map implementation to serve as a proxy for the
 * Redis persisted hash and provide lazy-loading semantics to minimize datastore
 * network traffic. It does a best-effort job of minimizing hash staleness but
 * does so without any locking and is not thread-safe. Only add and remove
 * operations trigger a remote-sync of local internal storage.
 */
public class RedisMap<K, V> implements Map<K, V> {
    private final Nest<? extends V> nest;
    private final Class<? extends K> keyClazz;
    private final Class<? extends V> valueClazz;
    private final Field field;
    private final Object owner;

    public RedisMap(final Class<? extends K> keyClazz,
            final Class<? extends V> valueClazz, final Nest<? extends V> nest,
            Field field, Object owner) {
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
        this.nest = nest;
        this.field = field;
        this.owner = owner;
    }

    private void indexValue(K element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            nest.cat(field.getName()).cat(element).sadd(
                    JOhmUtils.getId(owner).toString());
        }
    }

    private void unindexValue(K element) {
        if (field.isAnnotationPresent(Indexed.class)) {
            nest.cat(field.getName()).cat(element).srem(
                    JOhmUtils.getId(owner).toString());
        }
    }

    @Override
    public void clear() {
        Map<String, String> savedHash = nest.cat(JOhmUtils.getId(owner)).cat(
                field.getName()).hgetAll();
        for (Map.Entry<String, String> entry : savedHash.entrySet()) {
            nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).hdel(
                    entry.getKey());
        }
        nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).del();
    }

    @Override
    public boolean containsKey(Object key) {
        return scrollElements().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return scrollElements().containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return scrollElements().entrySet();
    }

    @Override
    public V get(Object key) {
        V value = null;
        String valueKey = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .hget(key.toString());
        if (!JOhmUtils.isNullOrEmpty(valueKey)) {
            value = JOhm.<V>get(valueClazz, Integer.parseInt(valueKey));
        }
        return value;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        Set<K> keys = new LinkedHashSet<K>();
        for (String key : nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .hkeys()) {
            keys.add((K) JOhmUtils.Convertor.convert(keyClazz, key));
        }
        return keys;
    }

    @Override
    public V put(K key, V value) {
        V previousValue = get(key);
        internalPut(key, value);
        return previousValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> mapToCopyIn) {
        for (Map.Entry<? extends K, ? extends V> entry : mapToCopyIn.entrySet()) {
            internalPut(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        V value = get(key);
        nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).hdel(
                key.toString());
        unindexValue((K) key);
        return value;
    }

    @Override
    public int size() {
        int repoSize = nest.cat(JOhmUtils.getId(owner)).cat(field.getName())
                .hlen();
        return repoSize;
    }

    @Override
    public Collection<V> values() {
        return scrollElements().values();
    }

    private V internalPut(final K key, final V value) {
        Map<String, String> hash = new LinkedHashMap<String, String>();
        hash.put(key.toString(), JOhmUtils.getId(value).toString());
        nest.cat(JOhmUtils.getId(owner)).cat(field.getName()).hmset(hash);
        indexValue(key);
        return value;
    }

    @SuppressWarnings("unchecked")
    private synchronized Map<K, V> scrollElements() {
        Map<String, String> savedHash = nest.cat(JOhmUtils.getId(owner)).cat(
                field.getName()).hgetAll();
        Map<K, V> backingMap = new HashMap<K, V>();
        for (Map.Entry<String, String> entry : savedHash.entrySet()) {
            K savedKey = (K) JOhmUtils.Convertor.convert(keyClazz, entry
                    .getKey());
            V savedValue = JOhm.<V>get(valueClazz, Integer.parseInt(entry
                    .getValue()));
            backingMap.put(savedKey, savedValue);
        }
        return backingMap;
    }
}
