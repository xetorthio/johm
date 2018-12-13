package redis.clients.johm;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.TransactionBlock;
import redis.clients.johm.collections.*;
import redis.clients.util.Pool;

/**
 * JOhm serves as the delegate responsible for heavy-lifting all mapping
 * operations between the Object Models at one end and Redis Persistence Models
 * on the other.
 */
public final class JOhm {
	private static Pool<Jedis> pool;

	/** Current db index, on new redis connection it is set by default to 0 */
	public static long dbIndex = 0L;

	/**
	 * Read the id from the given model. This operation will typically be useful
	 * only after an initial interaction with Redis in the form of a call to
	 * save().
	 */
	public static Long getId(final Object model) {
		return JOhmUtils.getId(model);
	}

	/**
	 * Check if given model is in the new state with an uninitialized id.
	 */
	public static boolean isNew(final Object model) {
		return JOhmUtils.isNew(model);
	}

	/**
	 * Check if the model with the given id exists in Redis.
	 *
	 * @param <T>
	 * @param clazz the entity class
	 * @param id    the entity id
	 * @return whether the entity exists in Redis.
	 */
	public static <T> boolean exists(Class<?> clazz, long id) {
		JOhmUtils.Validator.checkValidModelClazz(clazz);

		Nest nest = new Nest(clazz);
		nest.setPool(pool);
		return nest.cat(id).exists();
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
	public static <T> T get(Class<?> clazz, long id, String... ignoring) {
		JOhmUtils.Validator.checkValidModelClazz(clazz);

		Nest nest = new Nest(clazz);
		nest.setPool(pool);
		if (!nest.cat(id).exists()) {
			return null;
		}

		Object newInstance;
		try {
			newInstance = clazz.newInstance();
			JOhmUtils.loadId(newInstance, id);
			JOhmUtils.initCollections(newInstance, nest, ignoring);

			Map<String, String> hashedObject = nest.cat(id).hgetAll();
			List<String> ignoredProperties = Arrays.asList(ignoring);
			for (Field field : JOhmUtils.gatherAllFields(clazz, ignoring)) {
				if (ignoredProperties.contains(field.getName()))
					continue;

				fillField(hashedObject, newInstance, field, ignoring);
				fillArrayField(nest, newInstance, field);
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
	 * @param clazz          Class of Model annotated-type to search
	 * @param attributeName  Name of Model's attribute to search
	 * @param attributeValue Attribute's value to search in index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> find(Class<?> clazz, String attributeName,
	                               Object attributeValue, String... ignoring) {
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
		nest.setPool(pool);
		Set<String> modelIdStrings = nest.cat(attributeName)
				.cat(attributeValue).smembers();
		if (modelIdStrings != null) {
			// TODO: Do this lazy
			results = new ArrayList<Object>();
			Object indexed = null;
			for (String modelIdString : modelIdStrings) {
				indexed = get(clazz, Long.parseLong(modelIdString), ignoring);
				if (indexed != null) {
					results.add(indexed);
				}
			}
		}
		return (List<T>) results;
	}


	/**
	 * Search a Model in redis index using its attribute's given name/value
	 * pair. This can potentially return more than 1 matches if some indexed
	 * Model's have identical attributeValue for given attributeName.
	 *
	 * @param clazz      Class of Model annotated-type to search
	 * @param attributes The attributes you are searching
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> find(Class<?> clazz, NVField... attributes) {
		JOhmUtils.Validator.checkValidModelClazz(clazz);
		List<Object> results = null;

		Nest nest = new Nest(clazz);
		nest.setPool(pool);

		for (NVField pair : attributes) {
			String attributeName;
			if (!JOhmUtils.Validator.isIndexable(pair.getAttributeName())) {
				throw new InvalidFieldException();
			}

			try {
				Field field = clazz.getDeclaredField(pair.getAttributeName());
				field.setAccessible(true);
				if (!field.isAnnotationPresent(Indexed.class)) {
					throw new InvalidFieldException();
				}
				if (field.isAnnotationPresent(Reference.class)) {
					attributeName = JOhmUtils.getReferenceKeyName(field);
				} else {
					attributeName = pair.getAttributeName();
				}
			} catch (SecurityException e) {
				throw new InvalidFieldException();
			} catch (NoSuchFieldException e) {
				throw new InvalidFieldException();
			}
			if (JOhmUtils.isNullOrEmpty(pair.getAttributeValue())) {
				throw new InvalidFieldException();
			}
			nest.cat(attributeName)
					.cat(pair.getAttributeValue()).next();
		}
		Set<String> modelIdStrings = nest.sinter();


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
		if (!isNew(model)) {
			delete(model.getClass(), JOhmUtils.getId(model), true, saveChildren);
		}
		final Nest nest = initIfNeeded(model);

		final Map<String, String> hashedObject = new HashMap<String, String>();
		Map<RedisArray<Object>, Object[]> pendingArraysToPersist = null;
		try {
			String fieldName = null;
			for (Field field : JOhmUtils.gatherAllFields(model.getClass())) {
				field.setAccessible(true);
				if (JOhmUtils.detectJOhmCollection(field)
						|| field.isAnnotationPresent(Id.class)) {
					if (field.isAnnotationPresent(Id.class)) {
						JOhmUtils.Validator.checkValidIdType(field);
					}
					continue;
				}
				if (field.isAnnotationPresent(Array.class)) {
					Object[] backingArray = (Object[]) field.get(model);
					int actualLength = backingArray == null ? 0
							: backingArray.length;
					JOhmUtils.Validator.checkValidArrayBounds(field,
							actualLength);
					Array annotation = field.getAnnotation(Array.class);
					RedisArray<Object> redisArray = new RedisArray<Object>(
							annotation.length(), annotation.of(), nest, field,
							model);
					if (pendingArraysToPersist == null) {
						pendingArraysToPersist = new LinkedHashMap<RedisArray<Object>, Object[]>();
					}
					pendingArraysToPersist.put(redisArray, backingArray);
				}
				JOhmUtils.Validator.checkAttributeReferenceIndexRules(field);
				if (field.isAnnotationPresent(Attribute.class)) {
					fieldName = field.getName();
					Object fieldValueObject = field.get(model);
					if (fieldValueObject != null) {
						// date
						if (field.getType().equals(Date.class)) {
							try {
								hashedObject.put(fieldName,
									new SimpleDateFormat(field.getAnnotation(Attribute.class).date()).format((Date) fieldValueObject)
								);
							} catch (Throwable e) {
								// try with a fallback date format
								hashedObject.put(fieldName,
									new SimpleDateFormat(Attribute.DEFAULT_DATE_FORMAT).format((Date) fieldValueObject)
								);
							}
						}
						else
							hashedObject.put(fieldName, fieldValueObject.toString());
					}

				}
				if (field.isAnnotationPresent(Reference.class)) {
					fieldName = JOhmUtils.getReferenceKeyName(field);
					Object child = field.get(model);
					if (child != null) {
						if (JOhmUtils.getId(child) == null) {
							throw new MissingIdException(fieldName);
						}
						if (saveChildren) {
							save(child, saveChildren); // some more work to do
						}
						hashedObject.put(fieldName, String.valueOf(JOhmUtils.getId(child)));
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

		// this was in a nest.multi but as explained here https://github.com/xetorthio/jedis/pull/498
		// it has been deprecated.
		Long modelId = JOhmUtils.getId(model);
		// to support getAll
		if (model.getClass().isAnnotationPresent(SupportAll.class)) {
			nest.cat("all").sadd(String.valueOf(modelId));
		}
		nest.cat(modelId).del();
		nest.cat(modelId).hmset(hashedObject);
		// \\

		if (pendingArraysToPersist != null && pendingArraysToPersist.size() > 0) {
			for (Map.Entry<RedisArray<Object>, Object[]> arrayEntry : pendingArraysToPersist.entrySet()) {
				arrayEntry.getKey().write(arrayEntry.getValue());
			}
		}

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
	public static boolean delete(Class<?> clazz, long id) {
		return delete(clazz, id, true, false);
	}

    /**
     * Set expiration period of model
     * @param <T>
     * @param model
     * @param seconds
     * @return Long
     */
    public static <T> Long expire(T model, int seconds) {
        return expire(model, seconds, false);
    }
    
    /**
     * Set expiration period of model and indexes (optionally)
     * @param <T>
     * @param model
     * @param seconds
     * @param expireIndexes should indexes expire
     * @return Long
     */
    public static <T> Long expire(T model, int seconds, boolean expireIndexes) {
        Nest<T> nest = initIfNeeded(model);
        
        if (expireIndexes) {
            // think about promoting deleteChildren as default behavior so
            // that this field lookup gets folded into that
            // if-deleteChildren block
            for (Field field : JOhmUtils.gatherAllFields(model.getClass())) {
                if (field.isAnnotationPresent(Indexed.class)) {
                    field.setAccessible(true);
                    Object fieldValue = null;
                    try {
                        fieldValue = field.get(model);
                    } catch (IllegalArgumentException e) {
                        throw new JOhmException(e);
                    } catch (IllegalAccessException e) {
                        throw new JOhmException(e);
                    }
                    if (fieldValue != null
                            && field.isAnnotationPresent(Reference.class)) {
                        fieldValue = JOhmUtils.getId(fieldValue);
                    }
                    if (!JOhmUtils.isNullOrEmpty(fieldValue)) {
                        nest.cat(field.getName()).cat(fieldValue).expire(seconds);
                    }
                }
            }
        }
        
        return nest.cat(JOhmUtils.getId(model)).expire(seconds);
    }

	@SuppressWarnings("unchecked")
	public static boolean delete(Class<?> clazz, long id,
	                             boolean deleteIndexes, boolean deleteChildren) {
		JOhmUtils.Validator.checkValidModelClazz(clazz);
		boolean deleted = false;
		Object persistedModel = get(clazz, id);
		if (persistedModel != null) {
			Nest nest = new Nest(persistedModel);
			nest.setPool(pool);
			if (deleteIndexes) {
				// think about promoting deleteChildren as default behavior so
				// that this field lookup gets folded into that
				// if-deleteChildren block
				for (Field field : JOhmUtils.gatherAllFields(clazz)) {
					if (field.isAnnotationPresent(Indexed.class)) {
						field.setAccessible(true);
						Object fieldValue = null;
						try {
							fieldValue = field.get(persistedModel);
						} catch (IllegalArgumentException e) {
							throw new JOhmException(e);
						} catch (IllegalAccessException e) {
							throw new JOhmException(e);
						}
						if (fieldValue != null
								&& field.isAnnotationPresent(Reference.class)) {
							fieldValue = JOhmUtils.getId(fieldValue);
						}
						if (!JOhmUtils.isNullOrEmpty(fieldValue)) {
							nest.cat(field.getName()).cat(fieldValue).srem(
									String.valueOf(id));
						}
					}
				}
			}
			if (deleteChildren) {
				for (Field field : JOhmUtils.gatherAllFields(clazz)) {
					if (field.isAnnotationPresent(Reference.class)) {
						field.setAccessible(true);
						try {
							Object child = field.get(persistedModel);
							if (child != null) {
								delete(child.getClass(),
										JOhmUtils.getId(child), deleteIndexes,
										deleteChildren); // children
							}
						} catch (IllegalArgumentException e) {
							throw new JOhmException(e);
						} catch (IllegalAccessException e) {
							throw new JOhmException(e);
						}
					}

					clearRedisCollection(field, nest, persistedModel);
				}
			}

			// now delete parent
			deleted = nest.cat(id).del() == 1;
		}
		return deleted;
	}

	/* TODO: refactor */
	private static void clearRedisCollection(Field field, Nest nest, Object persistedModel) {
		if (field.isAnnotationPresent(Array.class)) {
			field.setAccessible(true);
			Array annotation = field.getAnnotation(Array.class);
			new RedisArray(annotation.length(), annotation.of(), nest, field, persistedModel).clear();
		}
		if (field.isAnnotationPresent(CollectionList.class)) {
			field.setAccessible(true);
			CollectionList annotation = field.getAnnotation(CollectionList.class);
			new RedisList(annotation.of(), nest, field, persistedModel).clear();
		}
		if (field.isAnnotationPresent(CollectionSet.class)) {
			field.setAccessible(true);
			CollectionSet annotation = field.getAnnotation(CollectionSet.class);
			new RedisSet(annotation.of(), nest, field, persistedModel).clear();
		}
		if (field.isAnnotationPresent(CollectionSortedSet.class)) {
			field.setAccessible(true);
			CollectionSortedSet annotation = field.getAnnotation(CollectionSortedSet.class);
			new RedisSortedSet(annotation.of(), annotation.by(), nest, field, persistedModel).clear();
		}
		if (field.isAnnotationPresent(CollectionMap.class)) {
			field.setAccessible(true);
			CollectionMap annotation = field.getAnnotation(CollectionMap.class);
			new RedisMap(annotation.key(), annotation.value(), nest, field, persistedModel).clear();
		}
	}

	/**
	 * Inject JedisPool into JOhm. This is a mandatory JOhm setup operation.
	 *
	 * @param pool
	 */
    public static Pool<Jedis> setPool(final Pool<Jedis> pool) {
		JOhm.pool = pool;
		return pool;
	}

	private static void fillField(final Map<String, String> hashedObject,
	                              final Object newInstance, final Field field, String... ignoring)
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
				Long referenceId = Long.valueOf(serializedReferenceId);
				field.set(newInstance, get(field.getType(), referenceId, ignoring));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void fillArrayField(final Nest nest, final Object model,
	                                   final Field field) throws IllegalArgumentException,
			IllegalAccessException {
		if (field.isAnnotationPresent(Array.class)) {
			field.setAccessible(true);
			Array annotation = field.getAnnotation(Array.class);
			RedisArray redisArray = new RedisArray(annotation.length(),
					annotation.of(), nest, field, model);
			field.set(model, redisArray.read());
		}
	}

	@SuppressWarnings("unchecked")
	private static Nest initIfNeeded(final Object model) {
		Long id = JOhmUtils.getId(model);
		Nest nest = new Nest(model);
		nest.setPool(pool);
		if (id == null) {
			// lazily initialize id, nest, collections
			id = nest.cat("id").incr();
			JOhmUtils.loadId(model, id);
			JOhmUtils.initCollections(model, nest);
		}
		return nest;
	}

	@SuppressWarnings("unchecked")
	public static <T> Set<T> getAll(Class<?> clazz) {
		JOhmUtils.Validator.checkValidModelClazz(clazz);
        JOhmUtils.Validator.checkSupportAll(clazz);
		Set<Object> results = null;
		Nest nest = new Nest(clazz);
		nest.setPool(pool);
		Set<String> modelIdStrings = nest.cat("all").smembers();
		if (modelIdStrings != null) {
			results = new HashSet<Object>();
			Object indexed = null;
			for (String modelIdString : modelIdStrings) {
				indexed = get(clazz, Long.parseLong(modelIdString));
				if (indexed != null) {
					results.add(indexed);
				}
			}
		}
		return (Set<T>) results;
	}

	/**
	 * Select the current database.
	 */
	public static void selectDb(long dbIndex) {
		JOhm.dbIndex = dbIndex;
	}

	/**
	 * Flush the current database.
	 */
	public static void flushDb() {
		Jedis jedis = pool.getResource();
		try {
			if (!jedis.getDB().equals(JOhm.dbIndex))
				jedis.select((int) JOhm.dbIndex);
			jedis.flushDB();
		} finally {
			pool.returnResource(jedis);
		}
	}
}
