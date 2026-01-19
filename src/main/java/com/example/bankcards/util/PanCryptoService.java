package com.example.bankcards.util;

public interface PanCryptoService {

    //без decrypt так как в ТЗ этого не требовалось
    byte[] encrypt(String pan);
    String hash(String pan);
}
