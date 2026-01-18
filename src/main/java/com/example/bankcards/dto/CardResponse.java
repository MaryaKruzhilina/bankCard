package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;


import java.math.BigDecimal;
import java.util.UUID;

public record CardResponse(UUID cardId, String panMasked, short expiryMonth, short expiryYear,
                           StatusCard statusCard, BigDecimal balance) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                "**** **** **** " + card.getPanLastFourNumber(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getStatus(),
                card.getBalance()
        );
    }
}
