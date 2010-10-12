package redis.clients.johm.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import redis.clients.johm.JOhm;
import redis.clients.johm.JOhmUtils;
import redis.clients.johm.Model;
import redis.clients.johm.Nest;

/**
 * RedisMap is a JOhm-internal Map implementation to serve as a proxy for the
 * Redis persisted hash and provide lazy-loading semantics to minimize datastore
 * network traffic. It does a best-effort job of minimizing hash staleness but
 * does so without any locking and is not thread-safe. Only add and remove
 * operations trigger a remote-sync of local internal storage.
 */
public class RedisMap<K, V extends Model> implements Map<K, V> {
    private final Nest nest;
    private final Class<?> keyClazz;
    private final Class<? extends Model> valueClazz;

    public RedisMap(final Class<?> keyClazz,
            final Class<? extends Model> valueClazz, final Nest nest) {
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
        this.nest = nest;
    }

    @Override
    public void clear() {
        Map<String, String> savedHash = nest.hgetAll();
        for (Map.Entry<String, String> entry : savedHash.entrySet()) {
            nest.hdel(entry.getKey());
        }
        nest.del();
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
        String valueKey = nest.hget(key.toString());
        if (!JOhmUtils.isNullOrEmpty(valueKey)) {
            value = JOhm.get(valueClazz, Integer.parseInt(valueKey));
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
        for (String key : nest.hkeys()) {
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

    @Override
    public V remove(Object key) {
        V value = get(key);
        nest.hdel(key.toString());
        return value;
    }

    @Override
    public int size() {
        int repoSize = nest.hlen();
        return repoSize;
    }

    @Override
    public Collection<V> values() {
        return scrollElements().values();
    }

    private V internalPut(final K key, final V value) {
        Map<String, String> hash = new LinkedHashMap<String, String>();
        hash.put(key.toString(), value.getId().toString());
        nest.hmset(hash);

        return value;
    }

    @SuppressWarnings("unchecked")
    private synchronized Map<K, V> scrollElements() {
        Map<String, String> savedHash = nest.hgetAll();
        Map<K, V> backingMap = new HashMap<K, V>();
        for (Map.Entry<String, String> entry : savedHash.entrySet()) {
            K savedKey = (K) JOhmUtils.Convertor.convert(keyClazz, entry
                    .getKey());
            V savedValue = JOhm.get(valueClazz, Integer.parseInt(entry
                    .getValue()));
            backingMap.put(savedKey, savedValue);
        }
        return backingMap;
    }
}
