package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.crypto.PanCryptoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private Clock clock;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PanCryptoServiceImpl panCryptoService;

    private CardService cardService;

    private UUID ownerId;
    private UUID cardId;

    private Card card;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        card = new Card(
                cardId,
                ownerId,
                new byte[]{9, 9, 9},
                "hash",
                "1234",
                (short) 12,
                (short) 2026,
                StatusCard.ACTIVE,
                BigDecimal.valueOf(200)
        );

        // ВАЖНО: создаём сервис вручную, чтобы не было сюрпризов от @InjectMocks
        cardService = new CardService(cardRepository, panCryptoService, clock);
    }

    @Test
    @DisplayName("Should create card successfully")
    void shouldCreateCardSuccessfully() {
        String pan = "4111111111111234";
        String panHash = "hash_pan";
        byte[] encrypted = new byte[]{1, 2, 3};

        when(panCryptoService.hash(pan)).thenReturn(panHash);
        when(cardRepository.existsByPanHash(panHash)).thenReturn(false);
        when(panCryptoService.encrypt(pan)).thenReturn(encrypted);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        stubClock();

        Card created = cardService.create(ownerId, pan);

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository, times(1)).save(captor.capture());

        Card saved = captor.getValue();

        assertNotNull(created);
        assertNull(saved.getId());

        assertEquals(ownerId, saved.getOwnerId());
        assertArrayEquals(encrypted, saved.getPanEncryptedCard());
        assertEquals(panHash, saved.getPanHash());
        assertEquals("1234", saved.getPanLastFourNumber());

        // fixedClock = 2026-01-17 => expiryMonth=1, expiryYear=2031
        assertEquals((short) 1, saved.getExpiryMonth());
        assertEquals((short) 2031, saved.getExpiryYear());

        assertEquals(StatusCard.ACTIVE, saved.getStatus());
        assertEquals(0, saved.getBalance().compareTo(BigDecimal.ZERO));

        verify(panCryptoService, times(1)).hash(pan);
        verify(cardRepository, times(1)).existsByPanHash(panHash);
        verify(panCryptoService, times(1)).encrypt(pan);
    }

    @Test
    @DisplayName("Should throw exception when card already exists")
    void shouldThrowExceptionWhenCardAlreadyExists() {
        String pan = "4111111111111234";
        String panHash = "hash_pan";

        when(panCryptoService.hash(pan)).thenReturn(panHash);
        when(cardRepository.existsByPanHash(panHash)).thenReturn(true);

        assertThrows(CardAlreadyExistsException.class, () -> cardService.create(ownerId, pan));

        verify(panCryptoService, times(1)).hash(pan);
        verify(cardRepository, times(1)).existsByPanHash(panHash);
        verify(panCryptoService, never()).encrypt(anyString());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Should return user's card by id")
    void shouldReturnUsersCardById() {
        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        Card found = cardService.getMyById(ownerId, cardId);

        assertEquals(cardId, found.getId());
        assertEquals(ownerId, found.getOwnerId());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should throw exception when user's card is not found by id")
    void shouldThrowExceptionWhenUsersCardIsNotFoundById() {
        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getMyById(ownerId, cardId));

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should return user's cards with pagination")
    void shouldReturnUsersCardsWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<Card>(List.of(card), pageable, 1);

        when(cardRepository.findAllByOwnerId(ownerId, pageable)).thenReturn(page);

        Page<Card> result = cardService.getMyCards(ownerId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(cardId, result.getContent().get(0).getId());

        verify(cardRepository, times(1)).findAllByOwnerId(ownerId, pageable);
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should return user's cards filtered by status with pagination")
    void shouldReturnUsersCardsByStatusWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<Card>(List.of(card), pageable, 1);

        when(cardRepository.findAllByOwnerIdAndStatus(ownerId, StatusCard.ACTIVE, pageable)).thenReturn(page);

        Page<Card> result = cardService.getMyCardsByStatus(ownerId, StatusCard.ACTIVE, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(StatusCard.ACTIVE, result.getContent().get(0).getStatus());

        verify(cardRepository, times(1)).findAllByOwnerIdAndStatus(ownerId, StatusCard.ACTIVE, pageable);
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should block user's card on request")
    void shouldBlockUsersCardOnRequest() {
        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card updated = cardService.blockMyCard(ownerId, cardId);

        assertEquals(StatusCard.BLOCKED, updated.getStatus());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, times(1)).save(card);
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should throw exception when blocking request card is not found")
    void shouldThrowExceptionWhenBlockingRequestCardIsNotFound() {
        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.blockMyCard(ownerId, cardId));

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should update card status by admin")
    void shouldUpdateCardStatusByAdmin() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card updated = cardService.adminUpdateStatus(cardId, StatusCard.BLOCKED);

        assertEquals(StatusCard.BLOCKED, updated.getStatus());

        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(card);
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should throw exception when admin updates status for non-existent card")
    void shouldThrowExceptionWhenAdminUpdatesStatusForNonExistentCard() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.adminUpdateStatus(cardId, StatusCard.ACTIVE));

        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should delete card by admin when card exists")
    void shouldDeleteCardByAdminWhenCardExists() {
        when(cardRepository.existsById(cardId)).thenReturn(true);

        cardService.adminDelete(cardId);

        verify(cardRepository, times(1)).existsById(cardId);
        verify(cardRepository, times(1)).deleteById(cardId);
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent card by admin")
    void shouldThrowExceptionWhenDeletingNonExistentCardByAdmin() {
        when(cardRepository.existsById(cardId)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> cardService.adminDelete(cardId));

        verify(cardRepository, times(1)).existsById(cardId);
        verify(cardRepository, never()).deleteById(any(UUID.class));
        verifyNoInteractions(panCryptoService);
    }

    @Test
    @DisplayName("Should return all cards filtered by status for admin with pagination")
    void shouldReturnAllCardsByStatusForAdminWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<Card>(List.of(card), pageable, 1);

        when(cardRepository.findAllByStatus(StatusCard.ACTIVE, pageable)).thenReturn(page);

        Page<Card> result = cardService.adminGetAllByStatus(StatusCard.ACTIVE, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(cardId, result.getContent().get(0).getId());

        verify(cardRepository, times(1)).findAllByStatus(StatusCard.ACTIVE, pageable);
        verifyNoInteractions(panCryptoService);
    }
    private void stubClock() {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.parse("2026-01-17T10:00:00Z"));
    }
}