package kp.app;

/**
 * Created by IntelliJ IDEA.
 * User: kpelykh
 * Date: 8/23/11
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class IEEE754Helper {

    public static String longBits(long n) {
        long mask = 1L << 63;

        StringBuffer txtBuf = new StringBuffer();

        while (mask != 0) {
            String bit = ((mask & n) != 0) ? "1" : "0";
            txtBuf.append(bit);
            mask >>>= 1;
        }
        txtBuf.insert(12, ' ');
        txtBuf.insert(1, ' ');
        return txtBuf.toString();
    }

    private static String intBits(int n) {
        int mask = 1 << 31;

        StringBuffer txtBuf = new StringBuffer();

        while (mask != 0) {
            String bit = ((mask & n) != 0) ? "1" : "0";
            txtBuf.append(bit);
            mask >>>= 1;
        }
        txtBuf.insert(9, ' ');
        txtBuf.insert(1, ' ');
        return txtBuf.toString();
    }

    public static void printDetails(double dVal) {
        long n = Double.doubleToLongBits(dVal);
        String txtBits;
        String txtExp;
        int e;
        txtBits = longBits(n);

        StringBuffer sb = new StringBuffer();


        sb.append("       ").append("Double ").append(dVal).append("\n");
        sb.append("         ").append("IEEE754 Double Bits:\n");
        sb.append("         ").append(txtBits).append("\n");

        txtExp = txtBits.substring(2, 13);
        e = Integer.parseInt(txtExp, 2);
        sb.append("         ").append("Exponent= " + e + " (" + txtExp + ")\n");
        sb.append("         ").append("Exponent-1023= " + (e - 1023)).append("\n");
        sb.append("         ").append("Hex: " + Double.toHexString(dVal) + "\n");
        sb.append("         ").append("\n");
        System.out.print(sb.toString());
    }
}
