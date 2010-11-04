package redis.clients.johm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.JedisException;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.TransactionBlock;

/**
 * JOhm serves as the delegate responsible for heavy-lifting all mapping
 * operations between the Object Models at one end and Redis Persistence Models
 * on the other.
 */
public final class JOhm {
    private static JedisPool jedisPool;

    /**
     * Read the id from the given model. This operation will typically be useful
     * only after an initial interaction with Redis in the form of a call to
     * save().
     */
    public static Integer getId(final Object model) {
        return JOhmUtils.getId(model);
    }

    /**
     * Check if given model is in the new state with an uninitialized id.
     */
    public static boolean isNew(final Object model) {
        return JOhmUtils.isNew(model);
    }

    /**
     * Load the model persisted in Redis looking it up by its id and Class type.
     * 
     * @param <T>
     * @param clazz
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<?> clazz, int id) {
        JOhmUtils.Validator.checkValidModelClazz(clazz);

        Nest nest = new Nest(clazz);
        nest.setJedisPool(jedisPool);
        if (nest.cat(id).exists().intValue() == 0) {
            return null;
        }

        Object newInstance;
        try {
            newInstance = clazz.newInstance();
            JOhmUtils.loadId(newInstance, id);
            JOhmUtils.initCollections(newInstance, nest);

            Map<String, String> hashedObject = nest.cat(id).hgetAll();
            for (Field field : clazz.getDeclaredFields()) {
                fillField(hashedObject, newInstance, field);
            }
            for (Field field : clazz.getSuperclass().getDeclaredFields()) {
                fillField(hashedObject, newInstance, field);
            }

            return (T) newInstance;
        } catch (InstantiationException e) {
            throw new JOhmException(e);
        } catch (IllegalAccessException e) {
            throw new JOhmException(e);
        }
    }

    /**
     * Search a Model in redis index using its attribute's given name/value
     * pair. This can potentially return more than 1 matches if some indexed
     * Model's have identical attributeValue for given attributeName.
     * 
     * @param clazz
     *            Class of Model annotated-type to search
     * @param attributeName
     *            Name of Model's attribute to search
     * @param attributeValue
     *            Attribute's value to search in index
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> find(Class<?> clazz, String attributeName,
            Object attributeValue) {
        JOhmUtils.Validator.checkValidModelClazz(clazz);
        List<Object> results = null;
        if (!JOhmUtils.Validator.isIndexable(attributeName)) {
            throw new InvalidFieldException();
        }

        try {
            Field field = clazz.getDeclaredField(attributeName);
            field.setAccessible(true);
            if (!field.isAnnotationPresent(Indexed.class)) {
                throw new InvalidFieldException();
            }
            if (field.isAnnotationPresent(Reference.class)) {
                attributeName = JOhmUtils.getReferenceKeyName(field);
            }
        } catch (SecurityException e) {
            throw new InvalidFieldException();
        } catch (NoSuchFieldException e) {
            throw new InvalidFieldException();
        }
        if (JOhmUtils.isNullOrEmpty(attributeValue)) {
            throw new InvalidFieldException();
        }
        Nest nest = new Nest(clazz);
        nest.setJedisPool(jedisPool);
        Set<String> modelIdStrings = nest.cat(attributeName)
                .cat(attributeValue).smembers();
        if (modelIdStrings != null) {
            // TODO: Do this lazy
            results = new ArrayList<Object>();
            Object indexed = null;
            for (String modelIdString : modelIdStrings) {
                indexed = get(clazz, Integer.parseInt(modelIdString));
                if (indexed != null) {
                    results.add(indexed);
                }
            }
        }
        return (List<T>) results;
    }

    /**
     * Save given model to Redis. By default, this does not save all its child
     * annotated-models. If hierarchical persistence is desirable, use the
     * overloaded save interface.
     * 
     * @param <T>
     * @param model
     * @return
     */
    public static <T> T save(final Object model) {
        return JOhm.<T>save(model, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> T save(final Object model, boolean saveChildren) {
        final Nest nest = initIfNeeded(model);

        final Map<String, String> hashedObject = new HashMap<String, String>();
        List<Field> fields = new ArrayList<Field>();
        fields.addAll(Arrays.asList(model.getClass().getDeclaredFields()));
        fields.addAll(Arrays.asList(model.getClass().getSuperclass()
                .getDeclaredFields()));
        try {
            String fieldName = null;
            for (Field field : fields) {
                field.setAccessible(true);
                JOhmUtils.Validator.checkAttributeReferenceIndexRules(field);
                if (field.isAnnotationPresent(Attribute.class)) {
                    fieldName = field.getName();
                    Object fieldValueObject = field.get(model);
                    if (fieldValueObject != null) {
                        hashedObject
                                .put(fieldName, fieldValueObject.toString());
                    }

                }
                if (field.isAnnotationPresent(Reference.class)) {
                    fieldName = JOhmUtils.getReferenceKeyName(field);
                    Object child = field.get(model);
                    if (child != null) {
                        if (JOhmUtils.getId(child) == null) {
                            throw new MissingIdException();
                        }
                        if (saveChildren) {
                            save(child, saveChildren); // some more work to do
                        }
                        hashedObject.put(fieldName, String.valueOf(JOhmUtils
                                .getId(child)));
                    }
                }
                if (field.isAnnotationPresent(Indexed.class)) {
                    Object fieldValue = field.get(model);
                    if (fieldValue != null
                            && field.isAnnotationPresent(Reference.class)) {
                        fieldValue = JOhmUtils.getId(fieldValue);
                    }
                    if (!JOhmUtils.isNullOrEmpty(fieldValue)) {
                        nest.cat(fieldName).cat(fieldValue).sadd(
                                String.valueOf(JOhmUtils.getId(model)));
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new JOhmException(e);
        } catch (IllegalAccessException e) {
            throw new JOhmException(e);
        }

        nest.multi(new TransactionBlock() {
            public void execute() throws JedisException {
                del(nest.cat(JOhmUtils.getId(model)).key());
                hmset(nest.cat(JOhmUtils.getId(model)).key(), hashedObject);
            }
        });
        return (T) model;
    }

    /**
     * Delete Redis-persisted model as represented by the given model Class type
     * and id.
     * 
     * @param clazz
     * @param id
     * @return
     */
    public static boolean delete(Class<?> clazz, int id) {
        return delete(clazz, id, false);
    }

    @SuppressWarnings("unchecked")
    public static boolean delete(Class<?> clazz, int id, boolean deleteChildren) {
        JOhmUtils.Validator.checkValidModelClazz(clazz);
        boolean deleted = false;
        Object persistedModel = get(clazz, id);
        if (persistedModel != null) {
            if (deleteChildren) {
                List<Field> fields = new ArrayList<Field>();
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                fields.addAll(Arrays.asList(clazz.getSuperclass()
                        .getDeclaredFields()));
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Reference.class)) {
                        field.setAccessible(true);
                        try {
                            Object child = field.get(persistedModel);
                            if (child != null) {
                                delete(child.getClass(),
                                        JOhmUtils.getId(child), deleteChildren); // children
                            }
                        } catch (IllegalArgumentException e) {
                            throw new JOhmException(e);
                        } catch (IllegalAccessException e) {
                            throw new JOhmException(e);
                        }
                    }
                }
            }

            // now delete parent
            Nest nest = new Nest(persistedModel);
            nest.setJedisPool(jedisPool);
            deleted = nest.cat(id).del() == 1;
        }
        return deleted;
    }

    /**
     * Inject JedisPool into JOhm. This is a mandatory JOhm setup operation.
     * 
     * @param jedisPool
     */
    public static void setPool(final JedisPool jedisPool) {
        JOhm.jedisPool = jedisPool;
    }

    private static void fillField(final Map<String, String> hashedObject,
            final Object newInstance, final Field field)
            throws IllegalAccessException {
        JOhmUtils.Validator.checkAttributeReferenceIndexRules(field);
        if (field.isAnnotationPresent(Attribute.class)) {
            field.setAccessible(true);
            field.set(newInstance, JOhmUtils.Convertor.convert(field,
                    hashedObject.get(field.getName())));
        }
        if (field.isAnnotationPresent(Reference.class)) {
            field.setAccessible(true);
            String serializedReferenceId = hashedObject.get(JOhmUtils
                    .getReferenceKeyName(field));
            if (serializedReferenceId != null) {
                Integer referenceId = Integer.valueOf(serializedReferenceId);
                field.set(newInstance, get(field.getType(), referenceId));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Nest initIfNeeded(final Object model) {
        Integer id = JOhmUtils.getId(model);
        Nest nest = new Nest(model);
        nest.setJedisPool(jedisPool);
        if (id == null) {
            // lazily initialize id, nest, collections
            id = nest.cat("id").incr();
            JOhmUtils.loadId(model, id);
            JOhmUtils.initCollections(model, nest);
        }
        return nest;
    }

}