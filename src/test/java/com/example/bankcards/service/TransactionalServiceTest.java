package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionalServiceTest {
    @Mock
    public CardRepository cardRepository;
    @InjectMocks
    private TransactionalService transactionalService;

    UUID ownerId = UUID.randomUUID();
    UUID fromCardId = UUID.randomUUID();
    UUID toCardId = UUID.randomUUID();

    Card fromCard;
    Card toCard;



    @BeforeEach
    void setup() {
        fromCard = new Card(fromCardId, ownerId, new byte[]{}, "1010", "3498",
                (short) 12, (short) 2026, StatusCard.ACTIVE, BigDecimal.valueOf(200));
        toCard = new Card(toCardId, ownerId, new byte[]{}, "1020", "5732",
                (short) 12, (short) 2026, StatusCard.ACTIVE, BigDecimal.valueOf(0));
    }

    @Test
    @DisplayName("Should transfer money successfully between user's cards")
    void shouldTransferMoneySuccessfullyBetweenUsersCards() {
        TransferRequest req = new TransferRequest(fromCardId, toCardId, BigDecimal.valueOf(100));
        Mockito.when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(fromCard));
        Mockito.when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.of(toCard));

        transactionalService.transfer(ownerId, req);

        ArgumentCaptor<Card> captorCard = ArgumentCaptor.forClass(Card.class);

        verify(cardRepository, times(2)).save(captorCard.capture());

        List<Card> saved = captorCard.getAllValues();

        Card fromCardUpdate = null;
        Card toCardUpdate = null;

        for (Card card : saved) {
            if(card.getId().equals(fromCardId)) {
                fromCardUpdate = card;
            } else {
                if (card.getId().equals(toCardId)) {
                    toCardUpdate = card;
                }
            }
        }

        assertEquals(fromCardId, fromCardUpdate.getId());
        assertEquals(toCardId, toCardUpdate.getId());
        assertEquals(0, fromCardUpdate.getBalance().compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, toCardUpdate.getBalance().compareTo(BigDecimal.valueOf(100)));

        verify(cardRepository).findByIdAndOwnerId(fromCardId, ownerId);
        verify(cardRepository).findByIdAndOwnerId(toCardId, ownerId);
        verifyNoMoreInteractions(cardRepository);
    }
    @Test
    @DisplayName("Should throw exception when amount is less than zero")
    void shouldThrowExceptionWhenAmountIsLessThanZero() {
        TransferRequest req = new TransferRequest(fromCardId, toCardId, BigDecimal.valueOf(-10));

        InvalidTransferAmountException ex = assertThrows(InvalidTransferAmountException.class,
                () -> transactionalService.transfer(ownerId, req)
        );

        assertEquals("Amount must be > 0", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when amount is zero")
    void shouldThrowExceptionWhenAmountIsLessThanOrEqualToZero(){
        TransferRequest reqAmountZero = new TransferRequest(fromCardId, toCardId, BigDecimal.valueOf(0));

        InvalidTransferAmountException ex = assertThrows(InvalidTransferAmountException.class,
                () -> transactionalService.transfer(ownerId, reqAmountZero));
        assertEquals("Amount must be > 0", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when source and destination cards are the same")
    void shouldThrowExceptionWhenSourceAndDestinationCardsAreTheSame(){
        TransferRequest  req= new TransferRequest(fromCardId, fromCardId, BigDecimal.valueOf(100));

        InvalidTransferToSameCardException ex = assertThrows(InvalidTransferToSameCardException.class, () -> transactionalService.transfer(ownerId, req));
        assertEquals("Cannot transfer to the same card", ex.getMessage());
        verifyNoInteractions(cardRepository);
    }

    @Test
    @DisplayName("Should throw exception when FROM card is not found by ID")
    void shouldThrowExceptionWhenFromCardIsNotFoundById(){
        TransferRequest  req = new TransferRequest(fromCardId, toCardId, BigDecimal.valueOf(100));

        Mockito.when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.empty());

        CardNotFoundException ex = assertThrows(CardNotFoundException.class, () -> transactionalService.transfer(ownerId, req));
        assertEquals("Card not found", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when TO card is not found by ID")
    void shouldThrowExceptionWhenToCardIsNotFoundById(){
        TransferRequest  req = new TransferRequest(fromCardId, toCardId, BigDecimal.valueOf(100));

        Mockito.when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(fromCard));
        Mockito.when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.empty());

        CardNotFoundException ex = assertThrows(CardNotFoundException.class, () -> transactionalService.transfer(ownerId, req));
        assertEquals("Card not found", ex.getMessage());
    }

    @Test
    @DisplayName("Should fail if FROM card is not ACTIVE")
    void shouldFailTransferWhenFromCardIsNotActive(){
        TransferRequest  req = new TransferRequest(fromCardId, toCardId, BigDecimal.valueOf(100));

        fromCard.setStatus(StatusCard.BLOCKED);
        Mockito.when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(fromCard));
        Mockito.when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.of(toCard));

        CardNotActiveException ex = assertThrows(CardNotActiveException.class, () -> transactionalService.transfer(ownerId, req));
        assertEquals("Card is not active: **** **** **** " + fromCard.getPanLastFourNumber(), ex.getMessage());
    }

    @Test
    @DisplayName("Should fail if TO card is not ACTIVE")
    void shouldFailTransferWhenToCardIsNotActive(){
        TransferRequest  req = new TransferRequest(fromCardId, toCardId, BigDecimal.valueOf(100));

        toCard.setStatus(StatusCard.EXPIRED);
        Mockito.when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(fromCard));
        Mockito.when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.of(toCard));

        CardNotActiveException ex = assertThrows(CardNotActiveException.class, () -> transactionalService.transfer(ownerId, req));
        assertEquals("Card is not active: **** **** **** " + toCard.getPanLastFourNumber(), ex.getMessage());
    }

    @Test
    @DisplayName("Should fail if FROM card has insufficient funds")
        void shouldFailTransferWhenFromCardHasInsufficientFunds(){
        TransferRequest  req = new TransferRequest(fromCardId, toCardId, BigDecimal.valueOf(1000));

        Mockito.when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(fromCard));
        Mockito.when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.of(toCard));

        InsufficientFundsException ex = assertThrows(InsufficientFundsException.class, () -> transactionalService.transfer(ownerId, req));
        assertEquals("Not enough money to card: **** **** **** " + fromCard.getPanLastFourNumber(), ex.getMessage());
    }

}