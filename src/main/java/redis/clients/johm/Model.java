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
	return nest.cat("id").incr();
    }

    @SuppressWarnings("unchecked")
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
		if (field.isAnnotationPresent(Attribute.class)) {
		    field.setAccessible(true);
		    Object fieldValue = field.get(this);
		    if (fieldValue != null) {
			hashedObject
				.put(field.getName(), fieldValue.toString());
		    }
		}
		if (field.isAnnotationPresent(Reference.class)) {
		    field.setAccessible(true);
		    JOhm.checkValidReference(field);
		    Model model = (Model) field.get(this);
		    if (model != null) {
			hashedObject.put(JOhm.getReferenceFieldName(field),
				String.valueOf(model.getId()));
		    }
		}
	    }

	    nest.multi(new TransactionBlock() {
		public void execute() throws JedisException {
		    del(nest.cat(getId()).key());
		    hmset(nest.cat(getId()).key(), hashedObject);
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

    public String key() {
	return nest.cat(getId()).key();
    }

    public boolean delete() {
	return JOhm.delete(this.getClass(), getId());
    }
}