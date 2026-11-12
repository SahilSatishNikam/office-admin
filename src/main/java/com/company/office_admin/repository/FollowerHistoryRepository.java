package com.company.office_admin.repository;

import com.company.office_admin.entity.FollowerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerHistoryRepository extends JpaRepository<FollowerHistory, Long> {

    List<FollowerHistory> findByPeriodTagOrderByRecordedDateAsc(String periodTag);
}

