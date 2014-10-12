package redis.clients.johm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Attribute {
    /**
     * Date format if the attribute is of type Date.
     * @return the date format
     */
    public String date() default "yyyy-MM-dd";
}
