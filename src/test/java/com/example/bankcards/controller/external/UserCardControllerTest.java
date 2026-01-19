package com.example.bankcards.controller.external;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.exception.*;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCardController.class)
class UserCardControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CardService service;

    @MockitoBean
    private TransferService transferService;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID cardId = UUID.randomUUID();
    private final UUID toCardId = UUID.randomUUID();
    private Card card;

    private final String body = """
          {
          "fromCard": "%s",
          "toCard": "%s",
          "amount": 100
        }
        """.formatted(cardId, toCardId);

    @BeforeEach
    void setUp() {
        card = new Card(cardId, ownerId, new byte[]{}, "0000", "4950",
                (short) 12, (short) 2026, StatusCard.ACTIVE, BigDecimal.valueOf(100));
    }
    @Test
    @DisplayName("GET /cards/{cardId} — returns CardResponse (200) when the card belongs to the authenticated user")
    void getCardById_shouldReturnCardResponse_whenCardBelongsToAuthenticatedUser() throws Exception {
        when(service.getMyById(ownerId, cardId)).thenReturn(card);

        mvc.perform(get("/api/cards/{cardId}", cardId)
                        .header("X-User-Id", ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(cardId.toString()));
    }

    @Test
    @DisplayName("GET /cards/{cardId} — returns 404 when the card is not found")
    void getCardById_shouldReturn404_whenCardNotFound()  throws Exception{
        when(service.getMyById(ownerId, cardId)).thenThrow(new CardNotFoundException());

        mvc.perform(get("/api/cards/{cardId}", cardId)
                        .header("X-User-Id", ownerId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Card not found")));
    }

    @Test
    @DisplayName("GET /cards — returns a paginated page of the user's cards (200)")
    void getMyCards_shouldReturnPageOfCards_whenPaginationProvided() throws Exception{
        Card cardSecond = new Card(UUID.randomUUID(), ownerId, new byte[]{}, "0001", "5678",
                (short) 12, (short) 2026, StatusCard.ACTIVE, BigDecimal.valueOf(100));

        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").descending());
        Page<Card> page = new PageImpl<>(List.of(card, cardSecond), pageable, 2);

        when(service.getMyCards(eq(ownerId), any(Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,desc")
                        .header("X-User-Id", ownerId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].cardId").value(card.getId().toString()))
                .andExpect(jsonPath("$.content[1].cardId").value(cardSecond.getId().toString()))

                .andExpect(jsonPath("$.content[0].panMasked").value("**** **** **** " + card.getPanLastFourNumber()))
                .andExpect(jsonPath("$.content[0].expiryMonth").value((int) card.getExpiryMonth()))
                .andExpect(jsonPath("$.content[0].expiryYear").value((int) card.getExpiryYear()))
                .andExpect(jsonPath("$.content[0].statusCard").value(card.getStatus().name()))
                .andExpect(jsonPath("$.content[0].balance").value(card.getBalance().intValue()));
    }

    @Test
    @DisplayName("GET /cards — returns a paginated page of the user's cards filtered by status (200)")
    void getMyCards_shouldReturnFilteredPageOfCards_whenStatusFilterProvided() throws  Exception{
        Card cardSecond = new Card(UUID.randomUUID(), ownerId, new byte[]{}, "0001", "5678",
                (short) 12, (short) 2026, StatusCard.ACTIVE, BigDecimal.valueOf(100));

        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").descending());
        Page<Card> page = new PageImpl<>(List.of(card, cardSecond), pageable, 2);

        when(service.getMyCardsByStatus(eq(ownerId),eq(StatusCard.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/api/cards")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,desc")
                        .header("X-User-Id", ownerId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].cardId").value(card.getId().toString()))
                .andExpect(jsonPath("$.content[1].cardId").value(cardSecond.getId().toString()))

                .andExpect(jsonPath("$.content[0].panMasked").value("**** **** **** " + card.getPanLastFourNumber()))
                .andExpect(jsonPath("$.content[0].expiryMonth").value((int) card.getExpiryMonth()))
                .andExpect(jsonPath("$.content[0].expiryYear").value((int) card.getExpiryYear()))
                .andExpect(jsonPath("$.content[0].statusCard").value(card.getStatus().name()))
                .andExpect(jsonPath("$.content[0].balance").value(card.getBalance().intValue()));

    }

    @Test
    @DisplayName("PATCH /cards/{cardId}/block — blocks the card and returns CardResponse with BLOCK status (200)")
    void blockMyCard_shouldReturnBlockedCardResponse_whenRequestIsValid() throws Exception{
        card.setStatus(StatusCard.BLOCKED);
        when(service.blockMyCard(ownerId, cardId)).thenReturn(card);

        mvc.perform(patch("/api/cards/{cardId}/block", cardId)
                        .header("X-User-Id", ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardId").value(cardId.toString()))
                .andExpect(jsonPath("$.statusCard").value(StatusCard.BLOCKED.name()));

        verify(service, times(1)).blockMyCard(ownerId, cardId);
    }

    @Test
    @DisplayName("PATCH /cards/{cardId}/block — returns 404 when the card is not found")
    void blockMyCard_shouldReturn404_whenCardNotFound()  throws Exception{
        when(service.blockMyCard(ownerId, cardId)).thenThrow(new CardNotFoundException());

        mvc.perform(patch("/api/cards/{cardId}/block", cardId)
                        .header("X-User-Id", ownerId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Card not found")));

        verify(service, times(1)).blockMyCard(ownerId, cardId);
    }

    @Test
    @DisplayName("POST /cards/transfer — returns 204 when the transfer is successful")
    void transferForMyCard_shouldReturn200_whenTransferIsSuccessful()  throws Exception{
        ArgumentCaptor<TransferRequest> captor = ArgumentCaptor.forClass(TransferRequest.class);

        mvc.perform(post("/api/cards/transfer")
                        .header("X-User-Id", ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(transferService, times(1)).transfer(eq(ownerId), captor.capture());

        TransferRequest req = captor.getValue();
        assertEquals(cardId, req.fromCard());
        assertEquals(toCardId, req.toCard());
        assertEquals(new BigDecimal("100"), req.amount());

    }

    @Test
    @DisplayName("POST /cards/transfer — returns 404 Not Found when any card in the transfer request is not found")
    void transferForMyCard_shouldReturn404_whenAnyCardNotFound()  throws Exception{
        doThrow(new CardNotFoundException())
                .when(transferService)
                .transfer(eq(ownerId), any(TransferRequest.class));

        mvc.perform(post("/api/cards/transfer")
                        .header("X-User-Id", ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Card not found")));

        verify(transferService, times(1)).transfer(eq(ownerId), any(TransferRequest.class));
    }

    @Test
    @DisplayName("POST /cards/transfer — returns 409 Conflict when any card in the transfer request is not ACTIVE")
    void transferForMyCard_shouldReturn409_whenAnyCardIsNotActive() throws Exception{
        String lastNumCard = "5454";
        doThrow(new CardNotActiveException(lastNumCard))
                .when(transferService)
                .transfer(eq(ownerId), any(TransferRequest.class));

        mvc.perform(post("/api/cards/transfer")
                        .header("X-User-Id", ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Card is not active")))
                .andExpect(jsonPath("$.message").value(Matchers.containsString(lastNumCard)));

        verify(transferService, times(1)).transfer(eq(ownerId), any(TransferRequest.class));
    }

    @Test
    @DisplayName("POST /cards/transfer — returns 409 Conflict when sender card has insufficient funds")
    void transferForMyCard_shouldReturn409_whenInsufficientFunds()  throws Exception{
        String lastNumCard = "5454";
        doThrow(new InsufficientFundsException(lastNumCard))
                .when(transferService)
                .transfer(eq(ownerId), any(TransferRequest.class));

        mvc.perform(post("/api/cards/transfer")
                        .header("X-User-Id", ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Not enough money to card")))
                .andExpect(jsonPath("$.message").value(Matchers.containsString(lastNumCard)));

        verify(transferService, times(1)).transfer(eq(ownerId), any(TransferRequest.class));
    }

    @Test
    @DisplayName("POST /cards/transfer — returns 404 when transfer amount is not positive")
    void transferForMyCard_shouldReturn404_whenTransferAmountIsNotPositive() throws Exception{
        doThrow(new InvalidTransferAmountException())
                .when(transferService)
                .transfer(eq(ownerId), any(TransferRequest.class));

        mvc.perform(post("/api/cards/transfer")
                        .header("X-User-Id", ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Amount must be > 0")));

        verify(transferService, times(1)).transfer(eq(ownerId), any(TransferRequest.class));
    }

    @Test
    @DisplayName("POST /cards/transfer — returns 404 when sender and receiver cards are the same")
    void transferForMyCard_shouldReturn404_whenSenderAndReceiverCardsAreSame() throws Exception{
        doThrow(new InvalidTransferToSameCardException())
                .when(transferService)
                .transfer(eq(ownerId), any(TransferRequest.class));

        mvc.perform(post("/api/cards/transfer")
                        .header("X-User-Id", ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Cannot transfer to the same card")));

        verify(transferService, times(1)).transfer(eq(ownerId), any(TransferRequest.class));
    }

}