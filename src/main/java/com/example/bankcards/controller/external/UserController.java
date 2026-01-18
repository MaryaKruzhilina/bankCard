package com.example.bankcards.controller.external;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {
    private final CardService cardService;
    private final TransferService transferService;

    public UserController(CardService cardService, TransferService transferService) {
        this.cardService = cardService;
        this.transferService = transferService;
    }

    @GetMapping("/cards/{cardId}")
    public CardResponse getCardById(@PathVariable UUID cardId, @AuthenticationPrincipal Jwt principal) {
        UUID ownerId = UUID.fromString(principal.getClaimAsString("userId"));
        Card card = cardService.getMyById(ownerId, cardId);
        return CardResponse.from(card);
    }

    @GetMapping("/cards")
    public Page<CardResponse> getMyCards(@RequestParam(required = false) StatusCard status,
                                         Pageable pageable,
                                         @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(jwt.getClaimAsString("userId"));

        Page<Card> cards = (status == null) ? cardService.getMyCards(ownerId, pageable)
                : cardService.getMyCardsByStatus(ownerId, status, pageable);

        return cards.map(CardResponse::from);
    }
    @PatchMapping("/cards/{cardId}/block")
    public CardResponse blockMyCard(@PathVariable UUID cardId, @AuthenticationPrincipal Jwt principal) {
        UUID ownerId = UUID.fromString(principal.getClaimAsString("userId"));
        Card card = cardService.blockMyCard(ownerId, cardId);
        return CardResponse.from(card);
    }

}