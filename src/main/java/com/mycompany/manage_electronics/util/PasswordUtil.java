package com.mycompany.manage_electronics.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Băm mật khẩu bằng PBKDF2-HMAC-SHA256 (có salt ngẫu nhiên).
 * Định dạng lưu trong DB: pbkdf2$<số vòng lặp>$<salt base64>$<hash base64>
 * Mật khẩu cũ dạng văn bản thô (dữ liệu từ phiên bản trước) vẫn đăng nhập được
 * và được tự động nâng cấp sang dạng băm ở lần đăng nhập đầu tiên.
 */
public class PasswordUtil {
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 65536;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] h = pbkdf2(password.toCharArray(), salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(h);
    }

    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX + "$");
    }

    /** So khớp mật khẩu nhập vào với giá trị lưu trong DB (dạng băm hoặc dạng thô cũ). */
    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) return false;
        if (!isHashed(stored)) return stored.equals(password);
        String[] p = stored.split("\\$");
        if (p.length != 4) return false;
        try {
            int iter = Integer.parseInt(p[1]);
            byte[] salt = Base64.getDecoder().decode(p[2]);
            byte[] expected = Base64.getDecoder().decode(p[3]);
            byte[] actual = pbkdf2(password.toCharArray(), salt, iter);
            return java.security.MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] pw, byte[] salt, int iter) {
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return f.generateSecret(new PBEKeySpec(pw, salt, iter, KEY_BITS)).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }
}
