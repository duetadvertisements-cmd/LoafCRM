package javaServers.utility;

import java.security.SecureRandom;

public class OTPUtility {
    private static final SecureRandom random = new SecureRandom();

    public static String generateOTP() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}