package kp.app;

import junit.framework.TestCase;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kpelykh
 * Date: 8/23/11
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */

public class DoubleBinarySortingTest extends TestCase {

    public void testComparison() throws Exception {

        runTests(-10.0, 10.0, -1);
        runTests(-0.0, 0.0, -1);
        runTests(-0.0, 1.9000, -1);
        runTests(-0.0, -1.9000, 1);
        runTests(0.0, -1.9000, 1);
        runTests(0.0, 1.9000, -1);
        runTests(1.0, 1.0000000000000002, -1);
        runTests(1.0000000000000002, 1.0000000000000004, -1);
        runTests(1.0000000000000002, -2.0, 1);
        runTests(2.0, -2.0, 1);
        runTests(1.7976931348623157e+308, 2.2250738585072014e-308, 1); //max normal number vs min positive normal number
        runTests(1.7976931348623157e+308, 4.9406564584124654e-324, 1); //max normal number vs min subnormal number
        runTests(2.2250738585072009e-308, 4.9406564584124654e-324, 1); //max subnormal number vs min positive subnormal number
        runTests(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -1); //negative inf vs positive inf
        runTests(Double.NEGATIVE_INFINITY, -2.2250738585072014e-308, -1); //negative inf vs positive inf
        runTests(Double.NaN, Double.NaN, 0);
        runTests(Double.NaN, 10.0002, 1);
        runTests(Double.NaN, -2.2250738585072014e-308, 1);
        runTests(Double.NaN, 2.2250738585072014e-308, 1);
        runTests(Double.NEGATIVE_INFINITY, Double.NaN, -1); //negative inf vs NaN
        runTests(Double.POSITIVE_INFINITY, Double.NaN, -1); //negative inf vs NaN

        //test the same number expressed in different ways
        runTests(5.0, 0.05e2, 0);
        runTests(5.0, 5000e-3, 0);

        runTests(null, Double.NaN, -1);
        runTests(null, Double.NEGATIVE_INFINITY, -1);
        runTests(null, -0.0, -1);
        runTests(null, 0.0, -1);
        runTests(null, 1.7976931348623157e+308, -1);
        runTests(null, Double.POSITIVE_INFINITY, -1);

    }


    public void testDeSerialization() {
        assertEquals(-10.0, MyBytes.fromBytes(MyBytes.toBytes(-10.0)));
        assertEquals(-0.0, MyBytes.fromBytes(MyBytes.toBytes(-0.0)));
        assertEquals(0.0, MyBytes.fromBytes(MyBytes.toBytes(0.0)));
        assertEquals(1.0, MyBytes.fromBytes(MyBytes.toBytes(1.0)));
        assertEquals(1.0000000000000002, MyBytes.fromBytes(MyBytes.toBytes(1.0000000000000002)));
        assertEquals(1.7976931348623157e+308, MyBytes.fromBytes(MyBytes.toBytes(1.7976931348623157e+308)));
        assertEquals(2.2250738585072009e-308, MyBytes.fromBytes(MyBytes.toBytes(2.2250738585072009e-308)));
        assertEquals(4.9E-324, MyBytes.fromBytes(MyBytes.toBytes(4.9E-324)));
        assertEquals(Double.NEGATIVE_INFINITY, MyBytes.fromBytes(MyBytes.toBytes(Double.NEGATIVE_INFINITY)));
        assertEquals(Double.POSITIVE_INFINITY, MyBytes.fromBytes(MyBytes.toBytes(Double.POSITIVE_INFINITY)));
        assertEquals(Double.NaN, MyBytes.fromBytes(MyBytes.toBytes(Double.NaN)));
        assertEquals(null, MyBytes.fromBytes(MyBytes.toBytes(null)));
    }

    public void testSorting() {

        ImmutableBytesWritable[] unsorted = new ImmutableBytesWritable[]{
                new ImmutableBytesWritable(MyBytes.toBytes(Double.MAX_VALUE)),
                new ImmutableBytesWritable(MyBytes.toBytes(null)),
                new ImmutableBytesWritable(MyBytes.toBytes(1134.35)),
                new ImmutableBytesWritable(MyBytes.toBytes(Double.POSITIVE_INFINITY)),
                new ImmutableBytesWritable(MyBytes.toBytes(Double.MIN_NORMAL)),
                new ImmutableBytesWritable(MyBytes.toBytes(Double.NaN)),
                new ImmutableBytesWritable(MyBytes.toBytes(0.0)),
                new ImmutableBytesWritable(MyBytes.toBytes(-0.0)),
                new ImmutableBytesWritable(MyBytes.toBytes(10.0)),
                new ImmutableBytesWritable(MyBytes.toBytes(-2.2250738585072014e-308)),
                new ImmutableBytesWritable(MyBytes.toBytes(+0.0)),
                new ImmutableBytesWritable(MyBytes.toBytes(2.2250738585072009e-308)),
                new ImmutableBytesWritable(MyBytes.toBytes(Double.NEGATIVE_INFINITY))
        };

        List<ImmutableBytesWritable> list = new ArrayList(Arrays.asList(unsorted));

        int counter = 0;
        System.out.println("Unsorted list:");
        for (ImmutableBytesWritable writable : list) {
            System.out.println(counter++ + ": " + MyBytes.fromBytes(writable.get()));
        }

        Collections.sort(list, new ImmutableBytesWritable.Comparator());

        System.out.println("Sorted list:");

        List<Double> sorted = new ArrayList<Double>();

        for (ImmutableBytesWritable writable : list) {
            Double tmp = MyBytes.fromBytes(writable.get());
            System.out.println(counter++ + ": " + MyBytes.fromBytes(writable.get()));
            sorted.add(tmp);
        }

        assertEquals(sorted.get(0), null);
        assertEquals(sorted.get(1), Double.NEGATIVE_INFINITY);
        assertEquals(sorted.get(2), -2.2250738585072014e-308);
        assertEquals(sorted.get(3), -0.0);
        assertEquals(sorted.get(4), 0.0);
        assertEquals(sorted.get(5), 0.0);
        assertEquals(sorted.get(6), 2.225073858507201E-308);
        assertEquals(sorted.get(7), 2.2250738585072014E-308);
        assertEquals(sorted.get(8), 10.0);
        assertEquals(sorted.get(9), 1134.35);
        assertEquals(sorted.get(10), 1.7976931348623157E308);
        assertEquals(sorted.get(11), Double.POSITIVE_INFINITY);
        assertEquals(sorted.get(12), Double.NaN);

    }


    private void runTests(Double aDouble, Double bDouble, int signum)
            throws Exception {


        byte[] aBytes = MyBytes.toBytes(aDouble);
        byte[] bBytes = MyBytes.toBytes(bDouble);


        ImmutableBytesWritable a = new ImmutableBytesWritable(aBytes);
        ImmutableBytesWritable b = new ImmutableBytesWritable(bBytes);

        System.out.println("Comparing " + String.valueOf(aDouble) + ", with " + String.valueOf(bDouble));
        if (aDouble != null) {
            IEEE754Helper.printDetails(aDouble);
            System.out.println("         byte array: " + IEEE754Helper.longBits(MyBytes.toLong(aBytes)));
            System.out.println("         byte array: " + a);
            System.out.println("\n");
        }

        if (bDouble != null) {
            IEEE754Helper.printDetails(bDouble);
            System.out.println("         byte array: " + IEEE754Helper.longBits(MyBytes.toLong(bBytes)));
            System.out.println("         byte array: " + b);
            System.out.println("=======================================================");
        }

        doComparisonsOnObjects(a, b, signum);
        doComparisonsOnRaw(a, b, signum);
    }


    private int signum(int i) {
        if (i > 0) return 1;
        if (i == 0) return 0;
        return -1;
    }

    private void doComparisonsOnRaw(ImmutableBytesWritable a,
                                    ImmutableBytesWritable b,
                                    int expectedSignum)
            throws IOException {
        ImmutableBytesWritable.Comparator comparator =
                new ImmutableBytesWritable.Comparator();

        ByteArrayOutputStream baosA = new ByteArrayOutputStream();
        ByteArrayOutputStream baosB = new ByteArrayOutputStream();

        a.write(new DataOutputStream(baosA));
        b.write(new DataOutputStream(baosB));

        assertEquals(
                "Comparing " + a + " and " + b + " as raw",
                signum(comparator.compare(baosA.toByteArray(), 0, baosA.size(),
                        baosB.toByteArray(), 0, baosB.size())),
                expectedSignum);

        assertEquals(
                "Comparing " + a + " and " + b + " as raw (inverse)",
                -signum(comparator.compare(baosB.toByteArray(), 0, baosB.size(),
                        baosA.toByteArray(), 0, baosA.size())),
                expectedSignum);
    }

    private void doComparisonsOnObjects(ImmutableBytesWritable a,
                                        ImmutableBytesWritable b,
                                        int expectedSignum) {
        ImmutableBytesWritable.Comparator comparator =
                new ImmutableBytesWritable.Comparator();
        assertEquals(
                "Comparing " + a + " and " + b + " as objects",
                signum(comparator.compare(a, b)), expectedSignum);
        assertEquals(
                "Comparing " + a + " and " + b + " as objects (inverse)",
                -signum(comparator.compare(b, a)), expectedSignum);
    }


}
