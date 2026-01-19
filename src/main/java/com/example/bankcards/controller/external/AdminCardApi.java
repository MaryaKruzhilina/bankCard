package com.example.bankcards.controller.external;

import com.example.bankcards.controller.advice.ExceptionResponse;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.enums.StatusCard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Admin card", description = "Operations related to bank cards for admin")
public interface AdminCardApi {

    @Operation(summary = "Create card by user ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card created"),
            @ApiResponse(responseCode = "409",
                    description = "Card already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<CardResponse> createNewCard(@PathVariable UUID userId);

    @Operation(summary = "Update status card by card ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card status is update"),
            @ApiResponse(responseCode = "404",
                    description = "Card not found in database",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<CardResponse> updateStatusCard(@PathVariable UUID cardId,
                                                  @RequestParam StatusCard status);

    @Operation(summary = "Remove card by user ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card removed"),
            @ApiResponse(responseCode = "404",
                    description = "Card not found in database",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<Void> deleteCard(@PathVariable UUID cardId);

    @Operation(summary = "Get page with list card. Optional filter by status card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    Page<CardResponse> getAllByStatus(@RequestParam StatusCard status, @ParameterObject Pageable pageable);
}
