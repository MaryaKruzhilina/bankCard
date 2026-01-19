package com.example.bankcards.controller.external;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserCardController implements UserCardApi {

    private final CardService cardService;
    private final TransferService transferService;

    public UserCardController(CardService cardService, TransferService transferService) {
        this.cardService = cardService;
        this.transferService = transferService;
    }

    @GetMapping("/cards/{cardId}")
    public CardResponse getCardById(@PathVariable UUID cardId, @RequestHeader("X-User-Id") UUID ownerId) {

        Card card = cardService.getMyById(ownerId, cardId);
        return CardResponse.from(card);
    }

    @GetMapping("/cards")
    public Page<CardResponse> getMyCards(@RequestParam(required = false) StatusCard status,
                                         Pageable pageable,
                                         @RequestHeader("X-User-Id") UUID ownerId) {
        Page<Card> cards = (status == null) ? cardService.getMyCards(ownerId, pageable)
                : cardService.getMyCardsByStatus(ownerId, status, pageable);

        return cards.map(CardResponse::from);
    }

    @PatchMapping("/cards/{cardId}/block")
    public CardResponse blockMyCard(@PathVariable UUID cardId, @RequestHeader("X-User-Id") UUID ownerId) {
        Card card = cardService.blockMyCard(ownerId, cardId);
        return CardResponse.from(card);
    }

    @PostMapping("/cards/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferForMyCard(@RequestBody TransferRequest transferRequest, @RequestHeader("X-User-Id") UUID ownerId) {
        transferService.transfer(ownerId, transferRequest);
    }

}