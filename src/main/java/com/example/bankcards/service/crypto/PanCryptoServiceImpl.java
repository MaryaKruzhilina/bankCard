package com.example.bankcards.service.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PanCryptoServiceImpl implements PanCryptoService {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;

    private final SecretKey aesKey;
    private final SecureRandom random = new SecureRandom();
    private final String pepper;

    public PanCryptoServiceImpl(
            @Value("${card.crypto.aes-key-base64}") String aesKeyBase64,
            @Value("${card.crypto.hash-pepper:}") String pepper
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(aesKeyBase64);
        this.aesKey = new SecretKeySpec(keyBytes, AES);
        this.pepper = pepper;
    }

    @Override
    public byte[] encrypt(String pan) {
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_BITS, iv));

            byte[] ciphertext = cipher.doFinal(pan.getBytes(StandardCharsets.UTF_8));

            // формат хранения: [iv][ciphertext]
            return ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
        } catch (Exception e) {
            throw new IllegalStateException("PAN encryption failed", e);
        }
    }

    @Override
    public String hash(String pan) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((pan + pepper).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("PAN hashing failed", e);
        }
    }
}
