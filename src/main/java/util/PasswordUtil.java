package util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final SecureRandom random = new SecureRandom();

    private static final int ITERATIONS = 120_000;
    private static final int SALT_LEN = 16;
    private static final int KEY_LEN  = 32; // 256-bit

    // (회원가입/테스트데이터 만들 때 사용) plain -> "iters:salt:hash"
    public static String hashPassword(String plainPassword) {
        byte[] salt = new byte[SALT_LEN];
        random.nextBytes(salt);

        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LEN);

        return ITERATIONS + ":" +
                Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);
    }

    // 로그인 검증
    public static boolean verifyPassword(String plainPassword, String stored) {
        if (plainPassword == null || stored == null) return false;

        String[] parts = stored.split(":");
        if (parts.length != 3) return false;

        int iterations;
        try { iterations = Integer.parseInt(parts[0]); }
        catch (Exception e) { return false; }

        byte[] salt, expected;
        try {
            salt = Base64.getDecoder().decode(parts[1]);
            expected = Base64.getDecoder().decode(parts[2]);
        } catch (Exception e) {
            return false;
        }

        byte[] actual = pbkdf2(plainPassword.toCharArray(), salt, iterations, expected.length);
        return MessageDigest.isEqual(expected, actual);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLenBytes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLenBytes * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("PBKDF2 error", e);
        }
    }
}
