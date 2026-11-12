package com.company.office_admin.repository;

import com.company.office_admin.entity.FolderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRecordRepository extends JpaRepository<FolderRecord, Long> {
    List<FolderRecord> findByFolderIdOrderByCreatedAtDesc(Long folderId);
    List<FolderRecord> findByItemTypeInOrderByCreatedAtDesc(List<String> itemTypes);
    long countByFolderId(Long folderId);
    boolean existsByFolderIdAndItemTypeAndItemId(Long folderId, String itemType, Long itemId);
}

