package com.project.thelittlethings.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class HMACtokens {
    private static final String HMAC = "HmacSHA256";
  
    private static final String SECRET = "thelittlethingsASD";

    // token format: base64(username:expiry):signature
    public static String issueToken(String username, long ttlSeconds) {
        long expiry = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = username + ":" + expiry;
        String b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String sig = hmac(b64);
        return b64 + ":" + sig;
    }

    public static boolean validateToken(String token) {
        try {
            String[] parts = token.split(":");
            if (parts.length != 2) return false;
            String b64 = parts[0];
            String sig = parts[1];
            if (!hmac(b64).equals(sig)) return false;
            String payload = new String(Base64.getUrlDecoder().decode(b64), StandardCharsets.UTF_8);
            String[] p = payload.split(":" );
            if (p.length != 2) return false;
            long expiry = Long.parseLong(p[1]);
            return Instant.now().getEpochSecond() <= expiry;
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractUsername(String token) {
        try {
            String b64 = token.split(":")[0];
            String payload = new String(Base64.getUrlDecoder().decode(b64), StandardCharsets.UTF_8);
            return payload.split(":")[0];
        } catch (Exception e) {
            return null;
        }
    }

    private static String hmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
