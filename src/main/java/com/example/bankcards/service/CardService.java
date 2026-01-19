package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.PanCryptoServiceImpl;
import com.example.bankcards.util.PanGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class CardService {
    private static final int CARD_VALID_YEARS = 5;

    private final CardRepository cardRepository;
    private final PanCryptoServiceImpl panCryptoService;
    private final Clock clock;

    public CardService(CardRepository cardRepository, PanCryptoServiceImpl pan, Clock clock) {
        this.cardRepository = cardRepository;
        this.panCryptoService = pan;
        this.clock = clock;
    }

    // admin. создать карту пользователю
    @Transactional
    public Card create(UUID ownerId) {
        String pan = PanGenerator.generate();
        String panHash = panCryptoService.hash(pan);

        if (cardRepository.existsByPanHash(panHash)) {
            throw new CardAlreadyExistsException();
        }

        String last4 = last4(pan);
        byte[] encryptedPan = panCryptoService.encrypt(pan);

        LocalDate now = LocalDate.now(clock);
        short expiryMonth = (short) now.getMonthValue();
        short expiryYear = (short) (now.getYear() + CARD_VALID_YEARS);

        Card card = new Card(
                null,
                ownerId,
                encryptedPan,
                panHash,
                last4,
                expiryMonth,
                expiryYear,
                StatusCard.ACTIVE,
                BigDecimal.ZERO
        );

        return cardRepository.save(card);
    }

    // получить свою карту по id
    @Transactional(readOnly = true)
    public Card getMyById(UUID ownerId, UUID cardId) {
        return cardRepository.findByIdAndOwnerId(cardId, ownerId)
                .orElseThrow(CardNotFoundException::new);
    }

    // список своих карт (пагинация)
    @Transactional(readOnly = true)
    public Page<Card> getMyCards(UUID ownerId, Pageable pageable) {
        return cardRepository.findAllByOwnerId(ownerId, pageable);
    }

    // список своих карт + фильтр по статусу
    @Transactional(readOnly = true)
    public Page<Card> getMyCardsByStatus(UUID ownerId, StatusCard status, Pageable pageable) {
        return cardRepository.findAllByOwnerIdAndStatus(ownerId, status, pageable);
    }

    // запросить на блокировку
    @Transactional
    public Card blockMyCard(UUID ownerId, UUID cardId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, ownerId)
                .orElseThrow(CardNotFoundException::new);

        card.setStatus(StatusCard.BLOCKED);
        return cardRepository.save(card);
    }

    // admin. изменить статус карты
    @Transactional
    public Card adminUpdateStatus(UUID cardId, StatusCard status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(CardNotFoundException::new);

        card.setStatus(status);
        return cardRepository.save(card);
    }

    // admin. удалить карту
    @Transactional
    public void adminDelete(UUID cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException();
        }
        cardRepository.deleteById(cardId);
    }

    // admin. Смотреть с фильтром по статусу
    @Transactional(readOnly = true)
    public Page<Card> adminGetAllByStatus(StatusCard status, Pageable pageable) {
        return cardRepository.findAllByStatus(status, pageable);
    }

    private String last4(String pan) {
        return pan.substring(pan.length() - 4);
    }
}