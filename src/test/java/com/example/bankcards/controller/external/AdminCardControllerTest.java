package com.example.bankcards.controller.external;

import com.example.bankcards.controller.advice.RestExceptionHandler;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCardController.class)
@Import(RestExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false) // если у тебя есть Spring Security — это уберёт фильтры в mvc тесте
class AdminCardControllerTest{

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CardService cardService;

    @Test
    @DisplayName("POST /admin/cards/{userId} — creates new card (200)")
    void createNewCard_shouldReturnCardResponse_whenUserIdValid() throws Exception {
        UUID userId = UUID.randomUUID();

        Card created = new Card(
                UUID.randomUUID(),
                userId,
                new byte[]{},
                "0001",
                "4950",
                (short) 12,
                (short) 2026,
                StatusCard.ACTIVE,
                BigDecimal.valueOf(100)
        );

        when(cardService.create(eq(userId))).thenReturn(created);

        mvc.perform(post("/admin/cards/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardId").value(created.getId().toString()))
                .andExpect(jsonPath("$.statusCard").value("ACTIVE"));

        verify(cardService).create(userId);
    }

    @Test
    @DisplayName("POST /admin/cards/{userId} — returns 409 when card already exists")
    void createNewCard_shouldReturn409_whenCardAlreadyExists() throws Exception {
        UUID userId = UUID.randomUUID();

        when(cardService.create(eq(userId)))
                .thenThrow(new CardAlreadyExistsException());

        mvc.perform(post("/admin/cards/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // если у ExceptionResponse другие имена полей — поменяй тут
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        verify(cardService).create(userId);
    }

    @Test
    @DisplayName("PATCH /admin/cards/{cardId}/status — updates card status (200)")
    void updateStatusCard_shouldReturnUpdatedCardResponse_whenRequestValid() throws Exception {
        UUID cardId = UUID.randomUUID();

        Card updated = new Card(
                cardId,
                UUID.randomUUID(),
                new byte[]{},
                "0001",
                "4950",
                (short) 12,
                (short) 2026,
                StatusCard.BLOCKED,
                BigDecimal.valueOf(100)
        );

        when(cardService.adminUpdateStatus(eq(cardId), eq(StatusCard.BLOCKED)))
                .thenReturn(updated);

        mvc.perform(patch("/admin/cards/{cardId}/status", cardId)
                        .param("status", "BLOCKED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardId").value(cardId.toString()))
                .andExpect(jsonPath("$.statusCard").value("BLOCKED"));

        verify(cardService).adminUpdateStatus(cardId, StatusCard.BLOCKED);
    }

    @Test
    @DisplayName("PATCH /admin/cards/{cardId}/status — returns 404 when card not found")
    void updateStatusCard_shouldReturn404_whenCardNotFound() throws Exception {
        UUID cardId = UUID.randomUUID();

        when(cardService.adminUpdateStatus(eq(cardId), eq(StatusCard.BLOCKED)))
                .thenThrow(new CardNotFoundException());

        mvc.perform(patch("/admin/cards/{cardId}/status", cardId)
                        .param("status", "BLOCKED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(cardService).adminUpdateStatus(cardId, StatusCard.BLOCKED);
    }

    @Test
    @DisplayName("DELETE /admin/cards/{cardId} — deletes card (204)")
    void deleteCard_shouldReturnNoContent_whenCardIdValid() throws Exception {
        UUID cardId = UUID.randomUUID();

        // void -> ничего не надо when/then
        mvc.perform(delete("/admin/cards/{cardId}", cardId))
                .andExpect(status().isNoContent());

        verify(cardService).adminDelete(cardId);
    }

    @Test
    @DisplayName("GET /admin/cards?status=ACTIVE — returns page of cards by status (200)")
    void getAllByStatus_shouldReturnPage_whenRequestValid() throws Exception {
        Card c1 = new Card(UUID.randomUUID(), UUID.randomUUID(), new byte[]{}, "0001", "1111",
                (short) 12, (short) 2026, StatusCard.ACTIVE, BigDecimal.valueOf(100));
        Card c2 = new Card(UUID.randomUUID(), UUID.randomUUID(), new byte[]{}, "0001", "2222",
                (short) 12, (short) 2026, StatusCard.ACTIVE, BigDecimal.valueOf(200));

        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").descending());
        Page<Card> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        when(cardService.adminGetAllByStatus(eq(StatusCard.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/admin/cards")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].cardId").value(c1.getId().toString()))
                .andExpect(jsonPath("$.content[1].cardId").value(c2.getId().toString()))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        // опционально: убедимся, что статус реально ушёл в сервис
        verify(cardService).adminGetAllByStatus(eq(StatusCard.ACTIVE), any(Pageable.class));
    }
}