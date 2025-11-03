package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.LogisticProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LogisticProviderRepository extends JpaRepository<LogisticProvider, Long> {
    Optional<LogisticProvider> findByProviderName(String providerName);
    List<LogisticProvider> findByIsDeletedFalse();
}
