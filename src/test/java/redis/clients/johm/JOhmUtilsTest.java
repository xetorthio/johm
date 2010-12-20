package redis.clients.johm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import redis.clients.johm.models.Book;
import redis.clients.johm.models.Item;

public class JOhmUtilsTest extends Assert {
    @Test
    public void testGatherAllFields() {
        List<Field> itemFields = JOhmUtils.gatherAllFields(Item.class);
        List<Field> bookFields = Arrays.asList(Book.class.getDeclaredFields());
        List<Field> allFields = JOhmUtils.gatherAllFields(Book.class);
        assertEquals(allFields.size(), itemFields.size() + bookFields.size());

        List<Field> combinedFields = new ArrayList<Field>();
        combinedFields.addAll(itemFields);
        combinedFields.addAll(bookFields);
        assertTrue(allFields.containsAll(combinedFields));
    }
}
