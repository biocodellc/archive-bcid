package bcid;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;

/**
 * The BCID identifiers operate by equating encoded Strings directly to BigIntegers in the database.
 * The BigIntegers in the database are used for joining and linking on the back-end.
 * This class implements the encoders interface, taking a BigInteger and turning
 * it into an encoded BCID String, and conversely
 * will attempt to take a String and decode it into an BigInteger.
 */
public class bcidEncoder implements encoder {
    // Make all base64 encoding URL safe -- this constructor will remove equals ("=") as padding
    private Base64 base64 = new Base64(true);

    // Define the set of characters for encoding
    static protected char[] chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_="
                    .toCharArray();

    // Lookup table for converting base64 characters to value in range 0..63
    static protected byte[] codes = new byte[256];

    static {
        for (int i = 0; i < 256; i++) codes[i] = -1;
        for (int i = 'A'; i <= 'Z'; i++) codes[i] = (byte) (i - 'A');
        for (int i = 'a'; i <= 'z'; i++) codes[i] = (byte) (26 + i - 'a');
        for (int i = '0'; i <= '9'; i++) codes[i] = (byte) (52 + i - '0');
        codes['-'] = 62;
        codes['_'] = 63;
    }

    String prefix = null;

    /**
     * Instantiate the encoderBCID class by passing in a prefix to work with
     * @param prefix
     */
    public bcidEncoder(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Use Base64 encoding to turn BigIntegers numbers into Strings
     * We do this both to obfuscate integers used as identifiers and also to save some space.
     * Base64 is not necessarily the best for compressing numbers but it is well-known
     * The encoding presented here adds a check digit at the end of the string
     *
     * @param big
     * @return A String representation of this BigInteger
     */
    public String encode(BigInteger big) {
        CheckDigit checkDigit = new CheckDigit();
        String strVal = prefix + "/" + new String(base64.encode(big.toByteArray()));
        strVal = strVal.replace("\r\n", "");
        return checkDigit.generate(strVal);
    }

    /**
     * Base64 decode identifiers into integer representations.
     * 1. verify that the entire string is good w/ check digit
     * 2. Then base64 decode just the encoded piece of the string
     *
     * @param entireString
     * @return a BigIntgeger representation of this BCID
     */
    public BigInteger decode(String entireString) throws Exception {
        CheckDigit checkDigit = new CheckDigit();

        // Pull off potential last piece of string which would represent the local Identifier
        // The piece to decode is ark:/NAAN/bcidIdentifer (anything else after a last trailing "/" not decoded)
        StringBuilder sbEntireString = new StringBuilder();
        String bits[] = entireString.split("/");
        // just want the first 3 chunks between the "/"'s
        sbEntireString.append(bits[0] + "/" + bits[1] + "/" + bits[2]);
        if (bits.length > 3) {
            sbEntireString.append("/" + bits[3]);
        }
        String encodedString = sbEntireString.toString();

        // Validate using CheckDigit
        if (!checkDigit.verify(encodedString))
            throw new Exception(entireString + " does not verify");

        // Get just the encoded portion of the string minus the prefix
        String encodedPiece = encodedString.replaceFirst(prefix, "").replaceFirst("/", "");
        // Now check the Actual String, minus check Character
        String actualString = checkDigit.getCheckDigit(encodedPiece);
        // Now return the integer that was encoded here.
        return new BigInteger(base64.decode(actualString));
    }
}
