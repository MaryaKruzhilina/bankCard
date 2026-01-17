package com.example.bankcards.service.crypto;

public interface PanCryptoService {
    //без decrypt так как в ТЗ этого не требовалось
    byte[] encrypt(String pan);
    String hash(String pan);
}
