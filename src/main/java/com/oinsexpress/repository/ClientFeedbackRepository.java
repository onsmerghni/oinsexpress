package com.oinsexpress.repository;

import com.oinsexpress.entity.ClientFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClientFeedbackRepository extends JpaRepository<ClientFeedback, UUID> {

    List<ClientFeedback> findAllByOrderByCreatedAtDesc();
    List<ClientFeedback> findByPackageId(String packageId);

    // ═══════════════════════════════════════════════════════════
    // SCORING IA — Méthodes ajoutées
    // ═══════════════════════════════════════════════════════════

    /** Moyenne des notes d'un livreur depuis une date */
    @Query("SELECT AVG(f.rating) FROM ClientFeedback f " +
           "WHERE f.livreurId = :livreurId " +
           "AND f.createdAt >= :since")
    Double avgRatingByLivreurId(
        @Param("livreurId") String livreurId,
        @Param("since") LocalDateTime since);

    /** Compter avis d'un livreur depuis une date */
    long countByLivreurIdAndCreatedAtAfter(String livreurId, LocalDateTime since);
}
