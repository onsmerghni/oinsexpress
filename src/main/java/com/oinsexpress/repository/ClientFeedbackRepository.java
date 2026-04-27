package com.oinsexpress.repository;

import com.oinsexpress.entity.ClientFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientFeedbackRepository extends JpaRepository<ClientFeedback, UUID> {
    List<ClientFeedback> findAllByOrderByCreatedAtDesc();
    List<ClientFeedback> findByPackageId(String packageId);
}
