package com.library.auth;

import java.security.SecureRandom;
import java.util.Base64;

public class StateGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public static String generateState() {
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

}
