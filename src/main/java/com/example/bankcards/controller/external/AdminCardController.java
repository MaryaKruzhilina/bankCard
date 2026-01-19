package com.example.bankcards.controller.external;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminCardController {

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    //может кидать CardAlreadyExistsException.java
    @PostMapping("/cards/{userId}")
    public ResponseEntity<CardResponse> createNewCard(@PathVariable UUID userId) {
        return ResponseEntity.ok(CardResponse.from(cardService.create(userId)));
    }
    //может кидать CardNotFoundException
    @PatchMapping("/cards/{cardId}/status")
    public ResponseEntity<CardResponse> updateStatusCard(@PathVariable UUID cardId,
                                                         @RequestParam StatusCard status) {
        return  ResponseEntity.ok(CardResponse.from(cardService.adminUpdateStatus(cardId, status)));
    }
    //CardNotFoundException
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        cardService.adminDelete(cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public Page<CardResponse> getAllByStatus(@RequestParam StatusCard status, Pageable pageable) {
        Page<Card> pages = cardService.adminGetAllByStatus(status, pageable);
        return pages.map(CardResponse::from);
    }

}