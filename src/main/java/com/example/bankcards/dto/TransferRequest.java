package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(@NotNull UUID fromCard, @NotNull UUID toCard, @NotNull BigDecimal amount){

}
