package com.company.office_admin.service;

import com.company.office_admin.dto.*;
import com.company.office_admin.entity.DashboardFolder;
import com.company.office_admin.entity.FolderRecord;
import com.company.office_admin.entity.SocialAccount;
import com.company.office_admin.entity.SocialPost;
import com.company.office_admin.repository.DashboardFolderRepository;
import com.company.office_admin.repository.FolderRecordRepository;
import com.company.office_admin.repository.SocialAccountRepository;
import com.company.office_admin.repository.SocialPostRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FolderService {

    private final DashboardFolderRepository folderRepository;
    private final FolderRecordRepository recordRepository;
    private final SocialPostRepository postRepository;
    private final SocialAccountRepository accountRepository;

    public FolderService(DashboardFolderRepository folderRepository,
                         FolderRecordRepository recordRepository,
                         SocialPostRepository postRepository,
                         SocialAccountRepository accountRepository) {
        this.folderRepository = folderRepository;
        this.recordRepository = recordRepository;
        this.postRepository = postRepository;
        this.accountRepository = accountRepository;
    }

    @PostConstruct
    public void init() {
        seedDefaultFoldersIfEmpty();
        ensureDesignerVaultExists();
        ensureCreatorStudioExists();
    }

    @Transactional(readOnly = true)
    public List<FolderDto> getAllFolders() {
        return folderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toFolderDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<DashboardFolder> getFolderById(Long id) {
        return folderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<FolderRecordDto> getFolderRecords(Long folderId) {
        return recordRepository.findByFolderIdOrderByCreatedAtDesc(folderId).stream()
                .map(this::toRecordDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DashboardFolder createFolder(CreateFolderRequest request) {
        DashboardFolder folder = new DashboardFolder(
                request.getName() != null && !request.getName().trim().isEmpty() ? request.getName().trim() : "Untitled Folder",
                request.getDescription(),
                request.getColor() != null && !request.getColor().trim().isEmpty() ? request.getColor() : "#4f46e5",
                request.getIcon() != null && !request.getIcon().trim().isEmpty() ? request.getIcon() : "folder"
        );
        return folderRepository.save(folder);
    }

    @Transactional
    public Optional<DashboardFolder> updateFolder(Long id, CreateFolderRequest request) {
        return folderRepository.findById(id).map(folder -> {
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                folder.setName(request.getName().trim());
            }
            if (request.getDescription() != null) {
                folder.setDescription(request.getDescription().trim());
            }
            if (request.getColor() != null && !request.getColor().trim().isEmpty()) {
                folder.setColor(request.getColor());
            }
            if (request.getIcon() != null && !request.getIcon().trim().isEmpty()) {
                folder.setIcon(request.getIcon());
            }
            folder.setUpdatedAt(LocalDateTime.now());
            return folderRepository.save(folder);
        });
    }

    @Transactional
    public boolean deleteFolder(Long id) {
        if (folderRepository.existsById(id)) {
            folderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<FolderRecordDto> addRecord(Long folderId, AddRecordRequest request) {
        return folderRepository.findById(folderId).map(folder -> {
            String itemType = request.getItemType() != null ? request.getItemType() : "NOTE";
            if (request.getMediaUrl() != null && !request.getMediaUrl().isBlank() && "NOTE".equalsIgnoreCase(itemType)) {
                itemType = request.getMediaType() != null && request.getMediaType().toLowerCase().contains("video") ? "VIDEO" : "IMAGE";
            }

            FolderRecord record = new FolderRecord(
                    folder,
                    itemType,
                    request.getItemId(),
                    request.getTitle() != null ? request.getTitle() : "Untitled Record",
                    request.getSubtitle(),
                    request.getContent(),
                    request.getMediaUrl(),
                    request.getMediaType(),
                    request.getBadge() != null ? request.getBadge() : "Active",
                    request.getBadgeColor() != null ? request.getBadgeColor() : "primary"
            );
            FolderRecord saved = recordRepository.save(record);
            folder.setUpdatedAt(LocalDateTime.now());
            folderRepository.save(folder);
            return toRecordDto(saved);
        });
    }

    @Transactional
    public boolean deleteRecord(Long folderId, Long recordId) {
        return recordRepository.findById(recordId)
                .filter(record -> record.getFolder().getId().equals(folderId))
                .map(record -> {
                    recordRepository.delete(record);
                    folderRepository.findById(folderId).ifPresent(f -> {
                        f.setUpdatedAt(LocalDateTime.now());
                        folderRepository.save(f);
                    });
                    return true;
                }).orElse(false);
    }

    @Transactional
    public Optional<FolderRecordDto> tagPostToFolder(Long folderId, Long postId) {
        Optional<DashboardFolder> folderOpt = folderRepository.findById(folderId);
        Optional<SocialPost> postOpt = postRepository.findById(postId);

        if (folderOpt.isEmpty() || postOpt.isEmpty()) {
            return Optional.empty();
        }

        DashboardFolder folder = folderOpt.get();
        SocialPost post = postOpt.get();

        // Check if already tagged to avoid duplicate
        List<FolderRecord> existing = recordRepository.findByFolderIdOrderByCreatedAtDesc(folderId);
        for (FolderRecord rec : existing) {
            if ("POST".equalsIgnoreCase(rec.getItemType()) && postId.equals(rec.getItemId())) {
                return Optional.of(toRecordDto(rec));
            }
        }

        String badge = post.getStatus() != null ? post.getStatus() : "POST";
        String badgeColor = "PUBLISHED".equalsIgnoreCase(post.getStatus()) ? "success"
                : ("SCHEDULED".equalsIgnoreCase(post.getStatus()) ? "warning" : "info");

        String subtitle = post.getPlatform() + " • " + (post.getAccountHandle() != null ? post.getAccountHandle() : "");

        FolderRecord record = new FolderRecord(
                folder,
                "POST",
                post.getId(),
                post.getTitle(),
                subtitle,
                post.getCaption(),
                post.getMediaUrl(),
                post.getMediaType(),
                badge,
                badgeColor
        );

        FolderRecord saved = recordRepository.save(record);
        folder.setUpdatedAt(LocalDateTime.now());
        folderRepository.save(folder);
        return Optional.of(toRecordDto(saved));
    }

    @Transactional
    public Optional<FolderRecordDto> tagAccountToFolder(Long folderId, Long accountId) {
        Optional<DashboardFolder> folderOpt = folderRepository.findById(folderId);
        Optional<SocialAccount> accountOpt = accountRepository.findById(accountId);

        if (folderOpt.isEmpty() || accountOpt.isEmpty()) {
            return Optional.empty();
        }

        DashboardFolder folder = folderOpt.get();
        SocialAccount account = accountOpt.get();

        String subtitle = account.getPlatform() + " • " + (account.getFollowersCount() != null ? account.getFollowersCount() + " followers" : "");
        String badge = account.isConnected() ? "Connected" : "Disconnected";
        String badgeColor = account.isConnected() ? "success" : "secondary";

        FolderRecord record = new FolderRecord(
                folder,
                "ACCOUNT",
                account.getId(),
                account.getHandle(),
                subtitle,
                "Profile URL: " + account.getProfileUrl(),
                badge,
                badgeColor
        );

        FolderRecord saved = recordRepository.save(record);
        folder.setUpdatedAt(LocalDateTime.now());
        folderRepository.save(folder);
        return Optional.of(toRecordDto(saved));
    }

    @Transactional
    public void seedDefaultFoldersIfEmpty() {
        // No hardcoded sample folders or dummy records
    }

    @Transactional
    public DashboardFolder ensureDesignerVaultExists() {
        Optional<DashboardFolder> existing = folderRepository.findFirstByNameContainingIgnoreCase("Designer Vault");
        if (existing.isPresent()) {
            return existing.get();
        }
        DashboardFolder folder = new DashboardFolder(
                "Designer Vault",
                "Graphics, banner mockups, Figma assets, and brand design kits",
                "#ec4899",
                "palette"
        );
        return folderRepository.save(folder);
    }

    @Transactional
    public DashboardFolder ensureCreatorStudioExists() {
        Optional<DashboardFolder> existing = folderRepository.findFirstByNameContainingIgnoreCase("Creator Studio");
        if (existing.isPresent()) {
            return existing.get();
        }
        DashboardFolder folder = new DashboardFolder(
                "Creator Studio",
                "Video hooks, script outlines, caption variations, and viral copy drafts",
                "#f97316",
                "creator"
        );
        return folderRepository.save(folder);
    }

    @Transactional(readOnly = true)
    public List<FolderRecordDto> getDesignerRecords() {
        DashboardFolder folder = ensureDesignerVaultExists();
        return getFolderRecords(folder.getId());
    }

    @Transactional(readOnly = true)
    public List<FolderRecordDto> getCreatorRecords() {
        DashboardFolder folder = ensureCreatorStudioExists();
        return getFolderRecords(folder.getId());
    }

    @Transactional(readOnly = true)
    public DashboardFolder getDesignerFolder() {
        return ensureDesignerVaultExists();
    }

    @Transactional(readOnly = true)
    public DashboardFolder getCreatorFolder() {
        return ensureCreatorStudioExists();
    }

    public FolderDto toFolderDto(DashboardFolder folder) {
        return new FolderDto(
                folder.getId(),
                folder.getName(),
                folder.getDescription(),
                folder.getColor(),
                folder.getIcon(),
                recordRepository.findByFolderIdOrderByCreatedAtDesc(folder.getId()).size(),
                folder.getCreatedAtFormatted()
        );
    }

    public FolderRecordDto toRecordDto(FolderRecord record) {
        return new FolderRecordDto(
                record.getId(),
                record.getFolder() != null ? record.getFolder().getId() : null,
                record.getItemType(),
                record.getItemId(),
                record.getTitle(),
                record.getSubtitle(),
                record.getContent(),
                record.getMediaUrl(),
                record.getMediaType(),
                record.getBadge(),
                record.getBadgeColor(),
                record.getCreatedAtFormatted()
        );
    }
}
