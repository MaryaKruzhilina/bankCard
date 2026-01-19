package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.StatusCard;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "bank_card")
public class Card {

    public Card() {
    }

    public Card(UUID id, UUID ownerId, byte[] panEncryptedCard, String panHash,
                String panLastFourNumber, short expiryMonth, short expiryYear,
                StatusCard status, BigDecimal balance) {
        this.id = id;
        this.ownerId = ownerId;
        this.panEncryptedCard = panEncryptedCard;
        this.panHash = panHash;
        this.panLastFourNumber = panLastFourNumber;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.status = status;
        this.balance = balance;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "pan_encrypted", nullable = false)
    private byte[] panEncryptedCard;

    @Column(name = "pan_hash", nullable = false, length = 64, unique = true)
    private String panHash;

    @Column(name = "pan_last4", nullable = false, length = 4)
    private String panLastFourNumber;

    @Column(name = "expiry_month", nullable = false)
    private short expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private short expiryYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_card", nullable = false)
    private StatusCard status;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public byte[] getPanEncryptedCard() {
        return panEncryptedCard;
    }

    public void setPanEncryptedCard(byte[] panEncryptedCard) {
        this.panEncryptedCard = panEncryptedCard;
    }

    public String getPanHash() {
        return panHash;
    }

    public void setPanHash(String panHash) {
        this.panHash = panHash;
    }

    public String getPanLastFourNumber() {
        return panLastFourNumber;
    }

    public void setPanLastFourNumber(String panLastFourNumber) {
        this.panLastFourNumber = panLastFourNumber;
    }

    public short getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(short expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public short getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(short expiryYear) {
        this.expiryYear = expiryYear;
    }

    public StatusCard getStatus() {
        return status;
    }

    public void setStatus(StatusCard status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(id, card.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", panEncryptedCard=" + Arrays.toString(panEncryptedCard) +
                ", panHash='" + panHash + '\'' +
                ", panLastFourNumber='" + panLastFourNumber + '\'' +
                ", expiryMonth=" + expiryMonth +
                ", expiryYear=" + expiryYear +
                ", status=" + status +
                ", balance=" + balance +
                '}';
    }
}
