package com.oinsexpress.repository;

import com.oinsexpress.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {

    Optional<Position> findFirstByLivreurIdOrderByRecordedAtDesc(String livreurId);

    @Query("SELECT p FROM Position p WHERE p.recordedAt > :since " +
           "AND p.id IN (SELECT MAX(p2.id) FROM Position p2 WHERE p2.recordedAt > :since GROUP BY p2.livreurId)")
    List<Position> findLatestPositions(@Param("since") LocalDateTime since);

    @Query("SELECT p FROM Position p WHERE p.livreurId = :livreurId AND p.recordedAt > :since ORDER BY p.recordedAt DESC")
    List<Position> findHistoryByLivreur(@Param("livreurId") String livreurId, @Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT p.livreurId FROM Position p WHERE p.recordedAt > :since")
    List<String> findActiveLivreursSince(@Param("since") LocalDateTime since);
}
