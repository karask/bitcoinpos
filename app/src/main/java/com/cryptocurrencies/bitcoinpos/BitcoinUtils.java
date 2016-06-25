package com.cryptocurrencies.bitcoinpos;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by kostas on 24/6/2016.
 */
public class BitcoinUtils {
    private static BitcoinUtils ourInstance = new BitcoinUtils();

    public static BitcoinUtils getInstance() {
        return ourInstance;
    }

    public final static String showAddressInvalidMessage = "showAddressInvalidMessage";

    // valid characters
    private final static String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    private BitcoinUtils() {
    }

    public static boolean validateAddress(String address) {
        if (address.length() < 26 || address.length() > 35) return false;
        byte[] decoded = DecodeBase58(address, 58, 25);
        if (decoded == null) return false;

        byte[] hash = Sha256(decoded, 0, 21, 2);

        return Arrays.equals(Arrays.copyOfRange(hash, 0, 4), Arrays.copyOfRange(decoded, 21, 25));
    }

    private static byte[] DecodeBase58(String input, int base, int len) {
        byte[] output = new byte[len];
        for (int i = 0; i < input.length(); i++) {
            char t = input.charAt(i);

            int p = ALPHABET.indexOf(t);
            if (p == -1) return null;
            for (int j = len - 1; j > 0; j--, p /= 256) {
                p += base * (output[j] & 0xFF);
                output[j] = (byte) (p % 256);
            }
            if (p != 0) return null;
        }

        return output;
    }

    private static byte[] Sha256(byte[] data, int start, int len, int recursion) {
        if (recursion == 0) return data;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Arrays.copyOfRange(data, start, start + len));
            return Sha256(md.digest(), 0, 32, recursion - 1);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
