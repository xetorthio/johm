package redis.clients.johm;

import java.lang.reflect.Field;
import java.util.List;

import redis.clients.johm.collections.RedisList;
import redis.clients.johm.collections.RedisMap;
import redis.clients.johm.collections.RedisSet;

/**
 * JOhm's Model is the fundamental abstraction of a persistable entity in Redis.
 * Any object desired to be persisted to Redis, should extend this class and
 * declare corresponding persistable Attribute's, Reference's, and Indexed's.
 */
public class Model extends JOhm {
    @Attribute
    private Integer id = null;

    @SuppressWarnings("unchecked")
    public Model() {
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(CollectionList.class)) {
                    JOhmUtils.Validator.checkValidCollection(field);
                    Nest n = nest.cat(field.getName()).fork();
                    CollectionList annotation = field
                            .getAnnotation(CollectionList.class);
                    RedisList list = new RedisList<Model>(annotation.of(), n);
                    field.set(this, list);
                }
                if (field.isAnnotationPresent(CollectionSet.class)) {
                    JOhmUtils.Validator.checkValidCollection(field);
                    Nest n = nest.cat(field.getName()).fork();
                    CollectionSet annotation = field
                            .getAnnotation(CollectionSet.class);
                    RedisSet set = new RedisSet<Model>(annotation.of(), n);
                    field.set(this, set);
                }
                if (field.isAnnotationPresent(CollectionMap.class)) {
                    JOhmUtils.Validator.checkValidCollection(field);
                    Nest n = nest.cat(field.getName()).fork();
                    CollectionMap annotation = field
                            .getAnnotation(CollectionMap.class);
                    RedisMap map = new RedisMap<Object, Model>(
                            annotation.key(), annotation.value(), n);
                    field.set(this, map);
                }
            } catch (IllegalArgumentException e) {
                throw new InvalidFieldException();
            } catch (IllegalAccessException e) {
                throw new InvalidFieldException();
            }
        }
    }

    /**
     * Persist Model in its current state to Redis.
     * 
     * @return
     */
    public <T extends Model> T save() {
        return JOhm.save(this);
    }

    /**
     * Delete this Model from Redis.
     * 
     * @return Success status of deletion.
     */
    public boolean delete() {
        return delete(false);
    }

    /**
     * Delete this Model and all its children from Redis if deleteChildren flag
     * is set.
     * 
     * @param deleteChildren
     * @return
     */
    public boolean delete(boolean deleteChildren) {
        return JOhm.delete(this.getClass(), getId(), deleteChildren);
    }

    /**
     * Get the Model from Redis as specified by given id.
     * 
     * @param id
     * @return
     */
    public static <T extends Model> T get(int id) {
        throw new UnsupportedOperationException(
                "Get will soon be supported on Model, just not yet");
    }

    /**
     * Search a Model in redis index using its attribute's given name/value
     * pair. This can potentially return more than 1 matches if some indexed
     * Model's have identical attributeValue for given attributeName.
     * 
     * @param attributeName
     * @param attributeValue
     * @return
     */
    public static List<? extends Model> find(String attributeName,
            Object attributeValue) {
        throw new UnsupportedOperationException(
                "Find will soon be supported on Model, just not yet");
    }

    /**
     * Get the id of this Model.
     * 
     * @return
     */
    public Integer getId() {
        if (isNew()) {
            throw new MissingIdException();
        }
        return id;
    }

    @SuppressWarnings("unchecked")
    public void switchOffAsyncCollections() {
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(CollectionList.class)) {
                    RedisList list = (RedisList) field.get(this);
                    list.switchOffAsynchrony();
                }
                if (field.isAnnotationPresent(CollectionSet.class)) {
                    RedisSet set = (RedisSet) field.get(this);
                    set.switchOffAsynchrony();
                }
                if (field.isAnnotationPresent(CollectionMap.class)) {
                    RedisMap map = (RedisMap) field.get(this);
                    map.switchOffAsynchrony();
                }
            } catch (IllegalArgumentException e) {
                throw new InvalidFieldException();
            } catch (IllegalAccessException e) {
                throw new InvalidFieldException();
            }
        }
    }

    /**
     * Indicate if the model is new
     * 
     * @return
     */
    protected boolean isNew() {
        return id == null;
    }

    protected void initializeId() {
        id = nest.cat("id").incr();
    }
}