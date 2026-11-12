package com.company.office_admin.repository;

import com.company.office_admin.entity.SocialPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SocialPostRepository extends JpaRepository<SocialPost, Long> {

    List<SocialPost> findByStatusOrderByPublishedAtDesc(String status);

    List<SocialPost> findByStatusOrderByScheduledAtAsc(String status);

    List<SocialPost> findByStatusAndPlatformClassOrderByPublishedAtDesc(String status, String platformClass);

    long countByStatus(String status);

    long countByStatusAndPublishedAtBetween(String status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.likesCount + p.commentsCount + p.sharesCount), 0) FROM SocialPost p WHERE p.status = 'PUBLISHED'")
    Long sumTotalEngagement();

    @Query("SELECT COALESCE(SUM(p.reachCount), 0) FROM SocialPost p WHERE p.status = 'PUBLISHED'")
    Long sumTotalReach();

    @Query("SELECT COALESCE(SUM(p.likesCount), 0) FROM SocialPost p WHERE p.status = 'PUBLISHED'")
    Long sumTotalLikes();

    @Query("SELECT COALESCE(SUM(p.commentsCount), 0) FROM SocialPost p WHERE p.status = 'PUBLISHED'")
    Long sumTotalComments();

    @Query("SELECT COALESCE(SUM(p.sharesCount), 0) FROM SocialPost p WHERE p.status = 'PUBLISHED'")
    Long sumTotalShares();

    @Query("SELECT p FROM SocialPost p WHERE LOWER(p.caption) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.platform) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.createdAt DESC")
    List<SocialPost> searchPosts(@org.springframework.data.repository.query.Param("query") String query);
}
