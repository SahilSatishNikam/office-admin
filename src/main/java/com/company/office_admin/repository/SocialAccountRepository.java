package com.company.office_admin.repository;

import com.company.office_admin.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    List<SocialAccount> findByConnectedTrue();

    java.util.Optional<SocialAccount> findByPlatformIgnoreCase(String platform);

    java.util.Optional<SocialAccount> findByPlatformIgnoreCaseAndExternalId(String platform, String externalId);

    @Query("SELECT COALESCE(SUM(s.followersCount), 0) FROM SocialAccount s WHERE s.connected = true")
    Long sumTotalFollowers();

    @Query("SELECT COALESCE(AVG(s.engagementRate), 0.0) FROM SocialAccount s WHERE s.connected = true")
    Double averageEngagementRate();

    @Query("SELECT s FROM SocialAccount s WHERE LOWER(s.platform) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.handle) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<SocialAccount> searchAccounts(@org.springframework.data.repository.query.Param("query") String query);
}

