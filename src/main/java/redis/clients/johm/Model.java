package redis.clients.johm;

import java.util.List;

/**
 * JOhm's Model is the fundamental abstraction of a persistable entity in Redis.
 * Any object desired to be persisted to Redis, should extend this class and
 * declare corresponding persistable Attribute's, Reference's, and Indexed's.
 */
public class Model extends JOhm {
    @Attribute
    private Integer id = null;

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
        return id;
    }

    protected boolean isNew() {
        return id == null;
    }

    void initializeId() {
        id = nest.cat("id").incr();
    }

}