package redis.clients.johm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import redis.clients.johm.models.User;

public class ConvertorTest extends Assert {
    @Test
    public void testSupportedConvertorsUnsignedValues() {
        // boolean
        String value = "true";
        Object converted = JOhmUtils.Convertor.convert(Boolean.class, value);
        assertTrue(converted.getClass().equals(Boolean.class));
        assertEquals(Boolean.TRUE, converted);

        converted = JOhmUtils.Convertor.convert(boolean.class, value);
        assertTrue(converted.getClass().equals(Boolean.class));
        assertEquals(Boolean.TRUE, converted);

        // byte
        value = "50";
        converted = JOhmUtils.Convertor.convert(Byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("50"), converted);

        converted = JOhmUtils.Convertor.convert(byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("50"), converted);

        // char
        value = "J";
        converted = JOhmUtils.Convertor.convert(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        converted = JOhmUtils.Convertor.convert(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        value = "";
        converted = JOhmUtils.Convertor.convert(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        converted = JOhmUtils.Convertor.convert(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        // short
        value = "10";
        converted = JOhmUtils.Convertor.convert(Short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("10"), converted);

        converted = JOhmUtils.Convertor.convert(short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("10"), converted);

        // int
        value = "10";
        converted = JOhmUtils.Convertor.convert(Integer.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("10"), converted);

        converted = JOhmUtils.Convertor.convert(int.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("10"), converted);

        // float
        value = "10.0f";
        converted = JOhmUtils.Convertor.convert(Float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("10.0f"), converted);

        converted = JOhmUtils.Convertor.convert(float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("10.0f"), converted);

        // double
        value = "10.0d";
        converted = JOhmUtils.Convertor.convert(Double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("10.0d"), converted);

        converted = JOhmUtils.Convertor.convert(double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("10.0d"), converted);

        // long
        value = "100";
        converted = JOhmUtils.Convertor.convert(Long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("100"), converted);

        converted = JOhmUtils.Convertor.convert(long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("100"), converted);

        // bigdecimal
        value = "10.999";
        converted = JOhmUtils.Convertor.convert(BigDecimal.class, value);
        assertTrue(converted.getClass().equals(BigDecimal.class));
        assertEquals(BigDecimal.valueOf(10.999d), converted);

        // biginteger
        value = "10999";
        converted = JOhmUtils.Convertor.convert(BigInteger.class, value);
        assertTrue(converted.getClass().equals(BigInteger.class));
        assertEquals(BigInteger.valueOf(10999L), converted);
    }

    @Test
    public void testSupportedConvertorsSignedValues() {
        // byte
        String value = "-50";
        Object converted = JOhmUtils.Convertor.convert(Byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("-50"), converted);

        converted = JOhmUtils.Convertor.convert(byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("-50"), converted);

        // char
        value = "J";
        converted = JOhmUtils.Convertor.convert(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        converted = JOhmUtils.Convertor.convert(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        value = "";
        converted = JOhmUtils.Convertor.convert(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        converted = JOhmUtils.Convertor.convert(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        // short
        value = "-10";
        converted = JOhmUtils.Convertor.convert(Short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("-10"), converted);

        converted = JOhmUtils.Convertor.convert(short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("-10"), converted);

        // int
        value = "-10";
        converted = JOhmUtils.Convertor.convert(Integer.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("-10"), converted);

        converted = JOhmUtils.Convertor.convert(int.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("-10"), converted);

        // float
        value = "-10.0f";
        converted = JOhmUtils.Convertor.convert(Float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("-10.0f"), converted);

        converted = JOhmUtils.Convertor.convert(float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("-10.0f"), converted);

        // double
        value = "-10.0d";
        converted = JOhmUtils.Convertor.convert(Double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("-10.0d"), converted);

        converted = JOhmUtils.Convertor.convert(double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("-10.0d"), converted);

        // long
        value = "-100";
        converted = JOhmUtils.Convertor.convert(Long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("-100"), converted);

        converted = JOhmUtils.Convertor.convert(long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("-100"), converted);

        // bigdecimal
        value = "-10.999";
        converted = JOhmUtils.Convertor.convert(BigDecimal.class, value);
        assertTrue(converted.getClass().equals(BigDecimal.class));
        assertEquals(BigDecimal.valueOf(-10.999d), converted);

        // biginteger
        value = "-10999";
        converted = JOhmUtils.Convertor.convert(BigInteger.class, value);
        assertTrue(converted.getClass().equals(BigInteger.class));
        assertEquals(BigInteger.valueOf(-10999L), converted);

        // object
        value = "User";
        converted = JOhmUtils.Convertor.convert(User.class, value);
        assertTrue(converted.getClass().equals(String.class));
        assertEquals(value, converted);
    }

    @Test
    public void testUnsupportedConvertors() {
        String value = "10";
        Object converted = JOhmUtils.Convertor.convert(new int[] {}.getClass(),
                value);
        assertNull(converted);

        converted = JOhmUtils.Convertor.convert(Enum.class, value);
        assertNull(converted);

        converted = JOhmUtils.Convertor.convert(Collection.class, value);
        assertNull(converted);
    }
}
