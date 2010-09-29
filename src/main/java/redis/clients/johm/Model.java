package redis.clients.johm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.JedisException;
import redis.clients.jedis.TransactionBlock;

public class Model extends JOhm {
    @Attribute
    private Integer id = null;

    private Integer initializeId() {
	return nest().cat("id").incr();
    }

    public <T extends Model> T save() {
	try {
	    final Map<String, String> hashedObject = new HashMap<String, String>();

	    if (this.id == null) {
		this.id = initializeId();
	    }
	    List<Field> fields = new ArrayList<Field>();
	    fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
	    fields.addAll(Arrays.asList(this.getClass().getSuperclass()
		    .getDeclaredFields()));

	    for (Field field : fields) {
		if (field.getAnnotation(Attribute.class) != null) {
		    field.setAccessible(true);
		    hashedObject.put(field.getName(), field.get(this)
			    .toString());
		}
	    }
	    nest().multi(new TransactionBlock() {
		public void execute() throws JedisException {
		    nest().cat(getId()).del();
		    nest().cat(getId()).hmset(hashedObject);
		}
	    });
	    return (T) this;
	} catch (IllegalArgumentException e) {
	    throw new JOhmException(e);
	} catch (IllegalAccessException e) {
	    throw new JOhmException(e);
	}
    }

    public int getId() {
	if (id == null) {
	    throw new MissingIdException();
	}
	return id;
    }

    private Nest nest() {
	return getNest().cat(this.getClass().getSimpleName());
    }

    public String key() {
	return nest().cat(getId()).key();
    }
}