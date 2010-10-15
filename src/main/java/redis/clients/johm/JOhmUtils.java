package redis.clients.johm;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JOhmUtils {
    static String getReferenceKeyName(final Field field) {
        return field.getName() + "_id";
    }

    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj.toString().trim().length() == 0) {
            return true;
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
        static void checkValidReference(final Field field) {
            if (!field.getType().getClass().isInstance(Model.class)) {
                throw new JOhmException(field.getType().getSimpleName()
                        + " is not a subclass of Model");
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
            }
            if (isReference) {
                checkValidReference(field);
            }
            if (isIndexed) {
                if (isAttribute) {
                    return;
                }
                if (isReference) {
                    // Revisit this in the future
                    throw new UnsupportedOperationException(
                            "Reference indexing is not yet supported");
                }
                throw new UnsupportedOperationException(
                        "Indexing is not supported for unpersisted fields");
            }
        }
    }
}
