package kp.app;

/**
 * Created by IntelliJ IDEA.
 * User: kpelykh
 * Date: 8/23/11
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */

public class MyBytes {

    /**
     * Size of long in bytes
     */
    public static final int SIZEOF_LONG = Long.SIZE / Byte.SIZE;

    /**
     * A constant holding a NULL representation of Double object
     */
    public static final long NULL_MASK = 0L;
    public static final long NEGATIVE_ZERO_MASK = -9223372036854775808L;
    public static final long POSITIVE_ZERO_MASK = 0L;
    public static final long NEGATIVE_INFINITY = 0xfff0000000000000L;
    public static final long POSITIVE_INFINITY = 0x7ff0000000000000L;

//  When doubles are serialized byte array should be sorted in the following order:
/*
    null
    -Infinity
    -Normal Number
    -Subnumber
    -0.0
    +0.0
    +Subnumber
    +Normal Number
    +Infinity
    NaN
*/

    public static byte[] toBytes(Double dObj) {

        long bits;

        if (dObj == null) {
            bits = NULL_MASK;
        } else {

            bits = Double.doubleToRawLongBits(dObj);

            if (bits == NEGATIVE_INFINITY) {
                bits = 1L; // make it next after null
            } else if (dObj < 0) {
                //for negative flip all bits
                bits = ~bits;
            } else if (bits == NEGATIVE_ZERO_MASK) {
                //flip first bit and make it less than +0.0
                bits = 0x7FF0000000000000L;
            } else if (bits == POSITIVE_ZERO_MASK) {
                //flip first bit
                bits = bits ^ (1L << 63);
            } else if (dObj > 0 && bits != POSITIVE_INFINITY) {
                //for positive flip only first
                bits = bits ^ (1L << 63);
            } else if (bits == POSITIVE_INFINITY) {
                bits = 0xFFFFFFFFFFFFFFFEL;
            } else if (dObj.isNaN()) {
                //move NaN to the top
                bits = 0xFFFFFFFFFFFFFFFFL;
            }
        }

        return MyBytes.toBytes(bits);
    }



    /**
     * Convert a long value to a byte array using big-endian.
     *
     * @param val value to convert
     * @return the byte array
     */
    public static byte[] toBytes(long val) {
        byte[] b = new byte[8];
        for (int i = 7; i > 0; i--) {
            b[i] = (byte) val;
            val >>>= 8;
        }
        b[0] = (byte) val;
        return b;
    }

    /**
     * Converts a byte array to a long value. Reverses
     * {@link #toBytes(long)}
     *
     * @param bytes array
     * @return the long value
     */
    public static long toLong(byte[] bytes) {
        return toLong(bytes, 0, SIZEOF_LONG);
    }

    /**
     * Converts a byte array to a long value.
     *
     * @param bytes  array of bytes
     * @param offset offset into array
     * @param length length of data (must be {@link #SIZEOF_LONG})
     * @return the long value
     * @throws IllegalArgumentException if length is not {@link #SIZEOF_LONG} or
     *                                  if there's not enough room in the array at the offset indicated.
     */
    public static long toLong(byte[] bytes, int offset, final int length) {
        if (length != SIZEOF_LONG || offset + length > bytes.length) {
            throw explainWrongLengthOrOffset(bytes, offset, length, SIZEOF_LONG);
        }
        long l = 0;
        for (int i = offset; i < offset + length; i++) {
            l <<= 8;
            l ^= bytes[i] & 0xFF;
        }
        return l;
    }

    private static IllegalArgumentException
    explainWrongLengthOrOffset(final byte[] bytes,
                               final int offset,
                               final int length,
                               final int expectedLength) {
        String reason;
        if (length != expectedLength) {
            reason = "Wrong length: " + length + ", expected " + expectedLength;
        } else {
            reason = "offset (" + offset + ") + length (" + length + ") exceed the"
                    + " capacity of the array: " + bytes.length;
        }
        return new IllegalArgumentException(reason);
    }

    public static Double fromBytes(byte[] bytes) {
        long bits = toLong(bytes);

        //no need to deserialize NaN because it already has right value 0xffffffffffffffffL

        if (bits == NULL_MASK) {
            return null;
        } else if (bits == 1L) {
             bits = NEGATIVE_INFINITY;
        } else if (bits == 0x7FF0000000000000L) {
           bits = NEGATIVE_ZERO_MASK;
        } else if (bits == 0xFFFFFFFFFFFFFFFEL) {
            bits = POSITIVE_INFINITY;
        } else if ((bits & (1L<<63)) == 0) {
            // negative number, flip all bits
            bits = ~bits;
        } else {
            // positive number, flip the first bit
             bits = bits ^ (1L<<63);
        }

        return Double.longBitsToDouble(bits);
    }
}
