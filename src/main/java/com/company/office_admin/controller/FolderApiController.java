package com.company.office_admin.controller;

import com.company.office_admin.dto.AddRecordRequest;
import com.company.office_admin.dto.CreateFolderRequest;
import com.company.office_admin.dto.FolderDto;
import com.company.office_admin.dto.FolderRecordDto;
import com.company.office_admin.entity.DashboardFolder;
import com.company.office_admin.service.FolderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class FolderApiController {

    private final FolderService folderService;

    public FolderApiController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    public ResponseEntity<List<FolderDto>> getAllFolders() {
        return ResponseEntity.ok(folderService.getAllFolders());
    }

    @PostMapping
    public ResponseEntity<FolderDto> createFolder(@RequestBody CreateFolderRequest request) {
        DashboardFolder created = folderService.createFolder(request);
        return ResponseEntity.ok(folderService.toFolderDto(created));
    }

    @GetMapping("/designer")
    public ResponseEntity<Map<String, Object>> getDesignerVault() {
        DashboardFolder folder = folderService.getDesignerFolder();
        List<FolderRecordDto> records = folderService.getDesignerRecords();
        return ResponseEntity.ok(Map.of(
                "folder", folderService.toFolderDto(folder),
                "records", records
        ));
    }

    @PostMapping("/designer/records")
    public ResponseEntity<FolderRecordDto> addDesignerRecord(@RequestBody AddRecordRequest request) {
        DashboardFolder folder = folderService.getDesignerFolder();
        if (request.getItemType() == null || request.getItemType().isBlank()) {
            request.setItemType("DESIGN_ASSET");
        }
        return folderService.addRecord(folder.getId(), request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/creator")
    public ResponseEntity<Map<String, Object>> getCreatorStudio() {
        DashboardFolder folder = folderService.getCreatorFolder();
        List<FolderRecordDto> records = folderService.getCreatorRecords();
        return ResponseEntity.ok(Map.of(
                "folder", folderService.toFolderDto(folder),
                "records", records
        ));
    }

    @PostMapping("/creator/records")
    public ResponseEntity<FolderRecordDto> addCreatorRecord(@RequestBody AddRecordRequest request) {
        DashboardFolder folder = folderService.getCreatorFolder();
        if (request.getItemType() == null || request.getItemType().isBlank()) {
            request.setItemType("CREATOR_DRAFT");
        }
        return folderService.addRecord(folder.getId(), request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFolder(@PathVariable Long id) {
        return folderService.getFolderById(id).map(folder -> {
            List<FolderRecordDto> records = folderService.getFolderRecords(id);
            return ResponseEntity.ok(Map.of(
                    "folder", folderService.toFolderDto(folder),
                    "records", records
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderDto> updateFolder(@PathVariable Long id, @RequestBody CreateFolderRequest request) {
        return folderService.updateFolder(id, request)
                .map(f -> ResponseEntity.ok(folderService.toFolderDto(f)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFolder(@PathVariable Long id) {
        boolean deleted = folderService.deleteFolder(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Folder deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/records")
    public ResponseEntity<List<FolderRecordDto>> getFolderRecords(@PathVariable Long id) {
        return ResponseEntity.ok(folderService.getFolderRecords(id));
    }

    @PostMapping("/{id}/records")
    public ResponseEntity<FolderRecordDto> addRecord(@PathVariable Long id, @RequestBody AddRecordRequest request) {
        return folderService.addRecord(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}/records/{recordId}")
    public ResponseEntity<Map<String, Object>> deleteRecord(@PathVariable Long id, @PathVariable Long recordId) {
        boolean deleted = folderService.deleteRecord(id, recordId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Record removed from folder"));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/tag-post/{postId}")
    public ResponseEntity<Map<String, Object>> tagPost(@PathVariable Long id, @PathVariable Long postId) {
        return folderService.tagPostToFolder(id, postId).map(record ->
                ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Post filed into folder successfully!",
                        "record", record
                ))
        ).orElse(ResponseEntity.badRequest().body(Map.of("success", false, "message", "Folder or Post not found")));
    }

    @PostMapping("/{id}/tag-account/{accountId}")
    public ResponseEntity<Map<String, Object>> tagAccount(@PathVariable Long id, @PathVariable Long accountId) {
        return folderService.tagAccountToFolder(id, accountId).map(record ->
                ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Account filed into folder successfully!",
                        "record", record
                ))
        ).orElse(ResponseEntity.badRequest().body(Map.of("success", false, "message", "Folder or Account not found")));
    }
}

