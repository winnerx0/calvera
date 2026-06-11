package com.winnerx0.calvera.webhook.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@RequiredArgsConstructor
class WebhookSignatureVerifier {

    private static final String ALGORITHM = "HmacSHA256";

    private final WebhookSecretRepository webhookSecretRepository;

    @Value("${webhook.encryption.key}")
    private String encryptionKey;

    boolean verify(Long projectId, byte[] rawBody, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }

        return webhookSecretRepository.findByProjectId(projectId)
                .map(ws -> {
                    String plainSecret = Encryptors.delux(encryptionKey, ws.getSalt()).decrypt(ws.getSecret());
                    return verifyHmac(plainSecret, rawBody, signatureHeader.substring(7));
                })
                .orElse(false);
    }

    private boolean verifyHmac(String secret, byte[] rawBody, String expectedHex) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            String computedHex = bytesToHex(mac.doFinal(rawBody));
            return MessageDigest.isEqual(
                    computedHex.getBytes(StandardCharsets.UTF_8),
                    expectedHex.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
