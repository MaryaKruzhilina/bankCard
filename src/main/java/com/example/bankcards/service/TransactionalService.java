package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionalService {

    private final CardRepository cardRepository;

    public TransactionalService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }
    @Transactional
    public void transfer(UUID ownerId, TransferRequest req) {
        if (!(req.amount().compareTo(BigDecimal.ZERO) > 0)) {
            throw new InvalidTransferAmountException();
        }

        if (req.fromToCard().equals(req.toCard())){
            throw new InvalidTransferToSameCardException();
        }
        Card fromTransfer = cardRepository.findByIdAndOwnerId(req.fromToCard(), ownerId)
                .orElseThrow(CardNotFoundException::new);

        Card toTransfer = cardRepository.findByIdAndOwnerId(req.toCard(), ownerId)
                .orElseThrow(CardNotFoundException::new);

        if(fromTransfer.getStatus() != StatusCard.ACTIVE){
            throw new CardNotActiveException(fromTransfer.getPanLastFourNumber());
        } else if(toTransfer.getStatus() != StatusCard.ACTIVE){
            throw new CardNotActiveException(toTransfer.getPanLastFourNumber());
        }
        if (fromTransfer.getBalance().compareTo(req.amount()) < 0) {
            throw new InsufficientFundsException(fromTransfer.getPanLastFourNumber());
        }
        fromTransfer.setBalance(fromTransfer.getBalance().subtract(req.amount()));
        toTransfer.setBalance(toTransfer.getBalance().add(req.amount()));

        cardRepository.save(fromTransfer);
        cardRepository.save(toTransfer);
    }
}
