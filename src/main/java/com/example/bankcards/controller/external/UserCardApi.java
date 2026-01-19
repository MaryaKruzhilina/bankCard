package com.example.bankcards.controller.external;

import com.example.bankcards.controller.advice.ExceptionResponse;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.enums.StatusCard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "User card", description = "Operations related to bank cards for user")
public interface UserCardApi {

    @Operation(summary = "Get user's card by card id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User`s card was found"),
            @ApiResponse(responseCode = "404", description = "Card wasn`t found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    CardResponse getCardById(@PathVariable UUID cardId, @RequestHeader("X-User-Id") UUID ownerId);

    @Operation(summary = "Get page user's cards by user id")
    @ApiResponse(responseCode = "200", description = "Page user card`s  was found")
    Page<CardResponse> getMyCards(@RequestParam(required = false) StatusCard status, Pageable pageable,
                                  @RequestHeader("X-User-Id") UUID ownerId);

    @Operation(summary = "Block set on user`s card by user id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card was blocked"),
            @ApiResponse(responseCode = "404", description = "Card wasn`t blocked",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    CardResponse blockMyCard(@PathVariable UUID cardId, @RequestHeader("X-User-Id") UUID ownerId);

    @Operation(summary = "Create transfer many if user`s card")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transfer was completed"),

            @ApiResponse(responseCode = "400", description = "Invalid transfer request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),

            @ApiResponse(responseCode = "404", description = "Card not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),

            @ApiResponse(responseCode = "409", description = "Transfer cannot be completed due to business rules",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    void transferForMyCard(@RequestBody TransferRequest transferRequest, @RequestHeader("X-User-Id") UUID ownerId);
}
