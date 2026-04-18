package com.project.ratemaster.repository;

import com.project.ratemaster.model.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiClientRepository extends JpaRepository<ApiClient,String> {
    Optional<ApiClient> findByEmail(String email);
    List<ApiClient> findByApiKeyPrefixStartingWith(String prefix);
}
