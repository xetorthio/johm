package redis.clients.johm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import redis.clients.johm.models.User;

public class ConvertorTest extends Assert {
    @Test
    public void testSupportedConvertorsUnsignedValues() {
        // boolean
        String value = "true";
        Object converted = Convertor.string2object(Boolean.class, value);
        assertTrue(converted.getClass().equals(Boolean.class));
        assertEquals(Boolean.TRUE, converted);

        converted = Convertor.string2object(boolean.class, value);
        assertTrue(converted.getClass().equals(Boolean.class));
        assertEquals(Boolean.TRUE, converted);

        // byte
        value = "50";
        converted = Convertor.string2object(Byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("50"), converted);

        converted = Convertor.string2object(byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("50"), converted);

        // char
        value = "J";
        converted = Convertor.string2object(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        converted = Convertor.string2object(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        value = "";
        converted = Convertor.string2object(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        converted = Convertor.string2object(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        // short
        value = "10";
        converted = Convertor.string2object(Short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("10"), converted);

        converted = Convertor.string2object(short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("10"), converted);

        // int
        value = "10";
        converted = Convertor.string2object(Integer.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("10"), converted);

        converted = Convertor.string2object(int.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("10"), converted);

        // float
        value = "10.0f";
        converted = Convertor.string2object(Float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("10.0f"), converted);

        converted = Convertor.string2object(float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("10.0f"), converted);

        // double
        value = "10.0d";
        converted = Convertor.string2object(Double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("10.0d"), converted);

        converted = Convertor.string2object(double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("10.0d"), converted);

        // long
        value = "100";
        converted = Convertor.string2object(Long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("100"), converted);

        converted = Convertor.string2object(long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("100"), converted);

        // bigdecimal
        value = "10.999";
        converted = Convertor.string2object(BigDecimal.class, value);
        assertTrue(converted.getClass().equals(BigDecimal.class));
        assertEquals(BigDecimal.valueOf(10.999d), converted);

        // biginteger
        value = "10999";
        converted = Convertor.string2object(BigInteger.class, value);
        assertTrue(converted.getClass().equals(BigInteger.class));
        assertEquals(BigInteger.valueOf(10999L), converted);

        // Date
        value = "2011-07-23 22:20:10.064+0000";
        converted = Convertor.string2object(Date.class, value);
        assertTrue(converted.getClass().equals(Date.class));
        assertEquals(new Date(1311459610064L), converted);
    }

    @Test
    public void testSupportedConvertorsSignedValues() {
        // byte
        String value = "-50";
        Object converted = Convertor.string2object(Byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("-50"), converted);

        converted = Convertor.string2object(byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("-50"), converted);

        // char
        value = "J";
        converted = Convertor.string2object(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        converted = Convertor.string2object(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        value = "";
        converted = Convertor.string2object(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        converted = Convertor.string2object(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        // short
        value = "-10";
        converted = Convertor.string2object(Short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("-10"), converted);

        converted = Convertor.string2object(short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("-10"), converted);

        // int
        value = "-10";
        converted = Convertor.string2object(Integer.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("-10"), converted);

        converted = Convertor.string2object(int.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("-10"), converted);

        // float
        value = "-10.0f";
        converted = Convertor.string2object(Float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("-10.0f"), converted);

        converted = Convertor.string2object(float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("-10.0f"), converted);

        // double
        value = "-10.0d";
        converted = Convertor.string2object(Double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("-10.0d"), converted);

        converted = Convertor.string2object(double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("-10.0d"), converted);

        // long
        value = "-100";
        converted = Convertor.string2object(Long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("-100"), converted);

        converted = Convertor.string2object(long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("-100"), converted);

        // bigdecimal
        value = "-10.999";
        converted = Convertor.string2object(BigDecimal.class, value);
        assertTrue(converted.getClass().equals(BigDecimal.class));
        assertEquals(BigDecimal.valueOf(-10.999d), converted);

        // biginteger
        value = "-10999";
        converted = Convertor.string2object(BigInteger.class, value);
        assertTrue(converted.getClass().equals(BigInteger.class));
        assertEquals(BigInteger.valueOf(-10999L), converted);

        // object
        value = "User";
        converted = Convertor.string2object(User.class, value);
        assertTrue(converted.getClass().equals(String.class));
        assertEquals(value, converted);
    }

    @Test
    public void testUnsupportedConvertors() {
        String value = "10";
        Object converted = Convertor.string2object(new int[] {}.getClass(),
                value);
        assertNull(converted);

        converted = Convertor.string2object(Enum.class, value);
        assertNull(converted);

        converted = Convertor.string2object(Collection.class, value);
        assertNull(converted);
    }
}
