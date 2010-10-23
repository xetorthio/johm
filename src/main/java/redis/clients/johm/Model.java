package redis.clients.johm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JOhm's Model is the fundamental annotation for a persistable entity in Redis.
 * Any object desired to be persisted to Redis, should use this annotation and
 * declare corresponding persistable Attribute's, Reference's, and Indexed's,
 * and various Collections, if so needed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Model {

}
