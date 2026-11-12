package com.company.office_admin.repository;

import com.company.office_admin.entity.DashboardFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardFolderRepository extends JpaRepository<DashboardFolder, Long> {
    List<DashboardFolder> findAllByOrderByCreatedAtDesc();
    Optional<DashboardFolder> findFirstByNameContainingIgnoreCase(String name);
}

