package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.StatusCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card,UUID> {

    boolean existsByPanHash(String panHash);

    Optional<Card> findByPanHash(String panHash);

    Page<Card> findAllByOwnerId(UUID ownerId, Pageable pageable);

    // с проверкой что пользователь не просто запрашивает карту по id а именно ту что ему принадлежит
    Optional<Card> findByIdAndOwnerId(UUID id, UUID ownerId);

    // Та-же пагинация но + фильтр статуса карты
    Page<Card> findAllByOwnerIdAndStatus(UUID ownerId, StatusCard status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = :id and c.ownerId = :ownerId")
    Optional<Card> findForUpdateByIdAndOwnerId(UUID id, UUID ownerId);

    // Только для Admin. Запрос всех карт по статусу + пагинация
    Page<Card> findAllByStatus(StatusCard status, Pageable pageable);
}
