package redis.clients.johm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import redis.clients.johm.models.User;

public class ConvertorTest extends Assert {

    Converter converter = new ConverterImpl();

    enum TestEnum { TEST1, TEST2 }

    @Test
    public void testSupportedConvertorsUnsignedValues() {
        // boolean
        String value = "true";
        Object converted = converter.getAsObject(Boolean.class, value);
        assertTrue(converted.getClass().equals(Boolean.class));
        assertEquals(Boolean.TRUE, converted);

        converted = converter.getAsObject(boolean.class, value);
        assertTrue(converted.getClass().equals(Boolean.class));
        assertEquals(Boolean.TRUE, converted);

        // byte
        value = "50";
        converted = converter.getAsObject(Byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("50"), converted);

        converted = converter.getAsObject(byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("50"), converted);

        // char
        value = "J";
        converted = converter.getAsObject(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        converted = converter.getAsObject(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        value = "";
        converted = converter.getAsObject(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        converted = converter.getAsObject(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        // short
        value = "10";
        converted = converter.getAsObject(Short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("10"), converted);

        converted = converter.getAsObject(short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("10"), converted);

        // int
        value = "10";
        converted = converter.getAsObject(Integer.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("10"), converted);

        converted = converter.getAsObject(int.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("10"), converted);

        // float
        value = "10.0f";
        converted = converter.getAsObject(Float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("10.0f"), converted);

        converted = converter.getAsObject(float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("10.0f"), converted);

        // double
        value = "10.0d";
        converted = converter.getAsObject(Double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("10.0d"), converted);

        converted = converter.getAsObject(double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("10.0d"), converted);

        // long
        value = "100";
        converted = converter.getAsObject(Long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("100"), converted);

        converted = converter.getAsObject(long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("100"), converted);

        // bigdecimal
        value = "10.999";
        converted = converter.getAsObject(BigDecimal.class, value);
        assertTrue(converted.getClass().equals(BigDecimal.class));
        assertEquals(BigDecimal.valueOf(10.999d), converted);

        // biginteger
        value = "10999";
        converted = converter.getAsObject(BigInteger.class, value);
        assertTrue(converted.getClass().equals(BigInteger.class));
        assertEquals(BigInteger.valueOf(10999L), converted);

        // Date
        value = "1311459610064";
        converted = converter.getAsObject(Date.class, value);
        assertTrue(converted.getClass().equals(Date.class));
        assertEquals(new Date(1311459610064L), converted);

        // enum
        value = "TEST2";
        converted = converter.getAsObject(TestEnum.class, value);
        assertEquals(converted.getClass(), TestEnum.class);
        assertEquals(TestEnum.TEST2, converted);
    }

    @Test
    public void testSupportedConvertorsSignedValues() {
        // byte
        String value = "-50";
        Object converted = converter.getAsObject(Byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("-50"), converted);

        converted = converter.getAsObject(byte.class, value);
        assertTrue(converted.getClass().equals(Byte.class));
        assertEquals(Byte.valueOf("-50"), converted);

        // char
        value = "J";
        converted = converter.getAsObject(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        converted = converter.getAsObject(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('J'), converted);

        value = "";
        converted = converter.getAsObject(Character.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        converted = converter.getAsObject(char.class, value);
        assertTrue(converted.getClass().equals(Character.class));
        assertEquals(Character.valueOf('\u0000'), converted);

        // short
        value = "-10";
        converted = converter.getAsObject(Short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("-10"), converted);

        converted = converter.getAsObject(short.class, value);
        assertTrue(converted.getClass().equals(Short.class));
        assertEquals(Short.valueOf("-10"), converted);

        // int
        value = "-10";
        converted = converter.getAsObject(Integer.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("-10"), converted);

        converted = converter.getAsObject(int.class, value);
        assertTrue(converted.getClass().equals(Integer.class));
        assertEquals(Integer.valueOf("-10"), converted);

        // float
        value = "-10.0f";
        converted = converter.getAsObject(Float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("-10.0f"), converted);

        converted = converter.getAsObject(float.class, value);
        assertTrue(converted.getClass().equals(Float.class));
        assertEquals(Float.valueOf("-10.0f"), converted);

        // double
        value = "-10.0d";
        converted = converter.getAsObject(Double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("-10.0d"), converted);

        converted = converter.getAsObject(double.class, value);
        assertTrue(converted.getClass().equals(Double.class));
        assertEquals(Double.valueOf("-10.0d"), converted);

        // long
        value = "-100";
        converted = converter.getAsObject(Long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("-100"), converted);

        converted = converter.getAsObject(long.class, value);
        assertTrue(converted.getClass().equals(Long.class));
        assertEquals(Long.valueOf("-100"), converted);

        // bigdecimal
        value = "-10.999";
        converted = converter.getAsObject(BigDecimal.class, value);
        assertTrue(converted.getClass().equals(BigDecimal.class));
        assertEquals(BigDecimal.valueOf(-10.999d), converted);

        // biginteger
        value = "-10999";
        converted = converter.getAsObject(BigInteger.class, value);
        assertTrue(converted.getClass().equals(BigInteger.class));
        assertEquals(BigInteger.valueOf(-10999L), converted);

        // object
        value = "User";
        converted = converter.getAsObject(User.class, value);
        assertTrue(converted.getClass().equals(String.class));
        assertEquals(value, converted);

        // Date
        value = "-1311459610064";
        converted = converter.getAsObject(Date.class, value);
        assertTrue(converted.getClass().equals(Date.class));
        assertEquals(new Date(-1311459610064L), converted);
    }

    @Test
    public void testUnsupportedConvertors() {
        String value = "10";
        Object converted = converter.getAsObject(new int[] {}.getClass(),
                value);
        assertNull(converted);

        converted = converter.getAsObject(Collection.class, value);
        assertNull(converted);
    }
}
