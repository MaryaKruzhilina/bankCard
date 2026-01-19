package com.example.bankcards.controller.external;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminCardController implements AdminCardApi{

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }


    @PostMapping("/cards/{userId}")
    public ResponseEntity<CardResponse> createNewCard(@PathVariable UUID userId) {
        return ResponseEntity.ok(CardResponse.from(cardService.create(userId)));
    }

    @PatchMapping("/cards/{cardId}/status")
    public ResponseEntity<CardResponse> updateStatusCard(@PathVariable UUID cardId,
                                                         @RequestParam StatusCard status) {
        return  ResponseEntity.ok(CardResponse.from(cardService.adminUpdateStatus(cardId, status)));
    }

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