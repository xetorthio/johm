package redis.clients.johm;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.johm.collections.RedisList;
import redis.clients.johm.collections.RedisMap;
import redis.clients.johm.collections.RedisSet;
import redis.clients.johm.collections.RedisSortedSet;

public final class JOhmUtils {
    static String getReferenceKeyName(final Field field) {
        return field.getName() + "_id";
    }

    public static Integer getId(final Object model) {
        return getId(model, true);
    }

    public static Integer getId(final Object model, boolean checkValidity) {
        Integer id = null;
        if (model != null) {
            if (checkValidity) {
                Validator.checkValidModel(model);
            }
            id = Validator.checkValidId(model);
        }
        return id;
    }

    static boolean isNew(final Object model) {
        return getId(model) == null;
    }

    @SuppressWarnings("unchecked")
    static void initCollections(final Object model, final Nest<?> nest) {
        if (model == null || nest == null) {
            return;
        }
        for (Field field : model.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(CollectionList.class)) {
                    Validator.checkValidCollection(field);
                    List<Object> list = (List<Object>) field.get(model);
                    if (list == null) {
                        CollectionList annotation = field
                                .getAnnotation(CollectionList.class);
                        RedisList<Object> redisList = new RedisList<Object>(
                                annotation.of(), nest, field, model);
                        field.set(model, redisList);
                    }
                }
                if (field.isAnnotationPresent(CollectionSet.class)) {
                    Validator.checkValidCollection(field);
                    Set<Object> set = (Set<Object>) field.get(model);
                    if (set == null) {
                        CollectionSet annotation = field
                                .getAnnotation(CollectionSet.class);
                        RedisSet<Object> redisSet = new RedisSet<Object>(
                                annotation.of(), nest, field, model);
                        field.set(model, redisSet);
                    }
                }
                if (field.isAnnotationPresent(CollectionSortedSet.class)) {
                    Validator.checkValidCollection(field);
                    Set<Object> sortedSet = (Set<Object>) field.get(model);
                    if (sortedSet == null) {
                        CollectionSortedSet annotation = field
                                .getAnnotation(CollectionSortedSet.class);
                        RedisSortedSet<Object> redisSortedSet = new RedisSortedSet<Object>(
                                annotation.of(), annotation.by(), nest, field,
                                model);
                        field.set(model, redisSortedSet);
                    }
                }
                if (field.isAnnotationPresent(CollectionMap.class)) {
                    Validator.checkValidCollection(field);
                    Map<Object, Object> map = (Map<Object, Object>) field
                            .get(model);
                    if (map == null) {
                        CollectionMap annotation = field
                                .getAnnotation(CollectionMap.class);
                        RedisMap<Object, Object> redisMap = new RedisMap<Object, Object>(
                                annotation.key(), annotation.value(), nest,
                                field, model);
                        field.set(model, redisMap);
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new InvalidFieldException();
            } catch (IllegalAccessException e) {
                throw new InvalidFieldException();
            }
        }
    }

    static void loadId(final Object model, final Integer id) {
        if (model != null) {
            boolean idFieldPresent = false;
            for (Field field : model.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Id.class)) {
                    idFieldPresent = true;
                    Validator.checkValidIdType(field);
                    try {
                        field.set(model, id);
                    } catch (IllegalArgumentException e) {
                        throw new JOhmException(e);
                    } catch (IllegalAccessException e) {
                        throw new JOhmException(e);
                    }
                    break;
                }
            }
            if (!idFieldPresent) {
                throw new JOhmException(
                        "JOhm does not support a Model without an Id");
            }
        }
    }

    static boolean detectJOhmCollection(final Field field) {
        boolean isJOhmCollection = false;
        if (field.isAnnotationPresent(CollectionList.class)
                || field.isAnnotationPresent(CollectionSet.class)
                || field.isAnnotationPresent(CollectionSortedSet.class)
                || field.isAnnotationPresent(CollectionMap.class)) {
            isJOhmCollection = true;
        }
        return isJOhmCollection;
    }

    @SuppressWarnings("unchecked")
    public static boolean isNullOrEmpty(final Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj.getClass().equals(Collection.class)) {
            return ((Collection) obj).size() == 0;
        } else {
            if (obj.toString().trim().length() == 0) {
                return true;
            }
        }

        return false;
    }

    public final static class Convertor {
        static Object convert(final Field field, final String value) {
            return convert(field.getType(), value);
        }

        public static Object convert(final Class<?> type, final String value) {
            if (type.equals(Byte.class) || type.equals(byte.class)) {
                return new Byte(value);
            }
            if (type.equals(Character.class) || type.equals(char.class)) {
                if (!isNullOrEmpty(value)) {
                    return value.charAt(0);
                } else {
                    // This is the default value
                    return '\u0000';
                }
            }
            if (type.equals(Short.class) || type.equals(short.class)) {
                return new Short(value);
            }
            if (type.equals(Integer.class) || type.equals(int.class)) {
                if (value == null) {
                    return 0;
                }
                return new Integer(value);
            }
            if (type.equals(Float.class) || type.equals(float.class)) {
                if (value == null) {
                    return 0f;
                }
                return new Float(value);
            }
            if (type.equals(Double.class) || type.equals(double.class)) {
                return new Double(value);
            }
            if (type.equals(Long.class) || type.equals(long.class)) {
                return new Long(value);
            }
            if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                return new Boolean(value);
            }

            // Higher precision folks
            if (type.equals(BigDecimal.class)) {
                return new BigDecimal(value);
            }
            if (type.equals(BigInteger.class)) {
                return new BigInteger(value);
            }

            if (type.isEnum()) {
                // return Enum.valueOf(type, value);
                return null; // TODO: handle these
            }

            // Collections not yet supported
            if (type.equals(Collection.class)) {
                return null; // TODO: handle these
            }
            if (type.isArray()) {
                return null; // TODO: handle these
            }

            return value;
        }
    }

    static final class Validator {
        static void checkValidAttribute(final Field field) {
            Class<?> type = field.getType();
            if ((type.equals(Byte.class) || type.equals(byte.class))
                    || type.equals(Character.class) || type.equals(char.class)
                    || type.equals(Short.class) || type.equals(short.class)
                    || type.equals(Integer.class) || type.equals(int.class)
                    || type.equals(Float.class) || type.equals(float.class)
                    || type.equals(Double.class) || type.equals(double.class)
                    || type.equals(Long.class) || type.equals(long.class)
                    || type.equals(Boolean.class) || type.equals(boolean.class)
                    || type.equals(BigDecimal.class)
                    || type.equals(BigInteger.class)
                    || type.equals(String.class)) {
            } else {
                throw new JOhmException(field.getType().getSimpleName()
                        + " is not a JOhm-supported Attribute");
            }
        }

        static void checkValidReference(final Field field) {
            if (!field.getType().getClass().isInstance(Model.class)) {
                throw new JOhmException(field.getType().getSimpleName()
                        + " is not a subclass of Model");
            }
        }

        static Integer checkValidId(final Object model) {
            Integer id = null;
            boolean idFieldPresent = false;
            for (Field field : model.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Id.class)) {
                    idFieldPresent = true;
                    Validator.checkValidIdType(field);
                    try {
                        id = (Integer) field.get(model);
                    } catch (IllegalArgumentException e) {
                        throw new JOhmException(e);
                    } catch (IllegalAccessException e) {
                        throw new JOhmException(e);
                    }
                    break;
                }
            }
            if (!idFieldPresent) {
                throw new JOhmException(
                        "JOhm does not support a Model without an Id");
            }
            return id;
        }

        static void checkValidIdType(final Field field) {
            Class<?> type = field.getType().getClass();
            if (!type.isInstance(Integer.class) || !type.isInstance(int.class)) {
                throw new JOhmException(field.getType().getSimpleName()
                        + " is annotated an Id but is not an integer");
            }
        }

        static boolean isIndexable(final String attributeName) {
            // Prevent null/empty keys and null/empty values
            if (!isNullOrEmpty(attributeName)) {
                return true;
            } else {
                return false;
            }
        }

        static void checkValidModel(final Object model) {
            checkValidModelClazz(model.getClass());
        }

        static void checkValidModelClazz(final Class<?> modelClazz) {
            if (!modelClazz.isAnnotationPresent(Model.class)) {
                throw new JOhmException(
                        "Class pretending to be a Model but is not really annotated");
            }
            if (modelClazz.isInterface()) {
                throw new JOhmException(
                        "An interface cannot be annotated as a Model");
            }
        }

        static void checkValidCollection(final Field field) {
            boolean isList = false, isSet = false, isMap = false, isSortedSet = false;
            if (field.isAnnotationPresent(CollectionList.class)) {
                checkValidCollectionList(field);
                isList = true;
            }
            if (field.isAnnotationPresent(CollectionSet.class)) {
                checkValidCollectionSet(field);
                isSet = true;
            }
            if (field.isAnnotationPresent(CollectionSortedSet.class)) {
                checkValidCollectionSortedSet(field);
                isSortedSet = true;
            }
            if (field.isAnnotationPresent(CollectionMap.class)) {
                checkValidCollectionMap(field);
                isMap = true;
            }
            if (isList && isSet && isMap && isSortedSet) {
                throw new JOhmException(
                        field.getName()
                                + " can be declared a List or a Set or a SortedSet or a Map but not more than one type");
            }
        }

        static void checkValidCollectionList(final Field field) {
            if (!field.getType().getClass().isInstance(List.class)) {
                throw new JOhmException(field.getType().getSimpleName()
                        + " is not a subclass of List");
            }
        }

        static void checkValidCollectionSet(final Field field) {
            if (!field.getType().getClass().isInstance(Set.class)) {
                throw new JOhmException(field.getType().getSimpleName()
                        + " is not a subclass of Set");
            }
        }

        static void checkValidCollectionSortedSet(final Field field) {
            if (!field.getType().getClass().isInstance(Set.class)) {
                throw new JOhmException(field.getType().getSimpleName()
                        + " is not a subclass of Set");
            }
        }

        static void checkValidCollectionMap(final Field field) {
            if (!field.getType().getClass().isInstance(Map.class)) {
                throw new JOhmException(field.getType().getSimpleName()
                        + " is not a subclass of Map");
            }
        }

        static void checkValidArrayBounds(final Field field, int actualLength) {
            if (field.getAnnotation(Array.class).length() < actualLength) {
                throw new JOhmException(
                        field.getType().getSimpleName()
                                + " has an actual length greater than the expected annotated array bounds");
            }
        }

        static void checkAttributeReferenceIndexRules(final Field field) {
            boolean isAttribute = field.isAnnotationPresent(Attribute.class);
            boolean isReference = field.isAnnotationPresent(Reference.class);
            boolean isIndexed = field.isAnnotationPresent(Indexed.class);
            if (isAttribute) {
                if (isReference) {
                    throw new JOhmException(
                            field.getName()
                                    + " is both an Attribute and a Reference which is invalid");
                }
                if (isIndexed) {
                    if (!isIndexable(field.getName())) {
                        throw new InvalidFieldException();
                    }
                }
                if (field.getType().equals(Model.class)) {
                    throw new JOhmException(field.getType().getSimpleName()
                            + " is an Attribute and a Model which is invalid");
                }
                checkValidAttribute(field);
            }
            if (isReference) {
                checkValidReference(field);
            }
        }
    }
}
