package com.example.zerowaste.controller;

import com.example.zerowaste.model.Attachment;
import com.example.zerowaste.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Tag(name = "Attachment Controller", description = "Endpoints for managing dropbox attachments")
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload file to dropbox")
    public Attachment uploadAttachment(@RequestParam("file") MultipartFile file) {
        return attachmentService.save(file);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all files from dropbox paginated")
    public Page<Attachment> getAllAttachments(Pageable pageable) {
        return attachmentService.getAllAttachments(pageable);
    }

    @GetMapping("/{id}/view")
    @Operation(summary = "Stream file from dropbox by attachment id")
    public ResponseEntity<StreamingResponseBody> viewAttachment(@PathVariable Long id) {
        Attachment attachment = attachmentService.getAttachmentById(id);

        StreamingResponseBody responseBody = outputStream -> {
            attachmentService.streamAttachmentFile(id, outputStream);
        };

        MediaType mediaType = MediaType.parseMediaType(attachment.getMimeType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(responseBody);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete file from dropbox")
    public void deleteAttachmentById(@PathVariable Long id) {
        attachmentService.deleteById(id);
    }
}
