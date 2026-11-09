package com.example.zerowaste.service.impl;

import com.dropbox.core.v2.files.FileMetadata;
import com.example.zerowaste.model.Attachment;
import com.example.zerowaste.repository.AttachmentRepository;
import com.example.zerowaste.service.AttachmentService;
import com.example.zerowaste.service.DropboxService;
import jakarta.persistence.EntityNotFoundException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final DropboxService dropboxService;

    @Transactional
    @Override
    public Attachment save(MultipartFile file) {
        FileMetadata uploadedFile = dropboxService.uploadFile(file);

        Attachment attachment = new Attachment();
        attachment.setDropboxFileID(uploadedFile.getId());
        attachment.setFilename(file.getOriginalFilename());
        attachment.setMimeType(file.getContentType());
        attachment.setUploadDate(LocalDateTime.now());

        return attachmentRepository.save(attachment);
    }

    @Override
    public Page<Attachment> getAllAttachments(Pageable pageable) {
        return attachmentRepository.findAll(pageable);
    }

    @Override
    public Attachment getAttachmentById(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Attachment not found with id: " + id));
    }

    public void streamAttachmentFile(Long id, OutputStream outputStream) {
        Attachment attachment = getAttachmentById(id);
        dropboxService.fetchFile(attachment.getDropboxFileID(), outputStream);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        Attachment attachment = getAttachmentById(id);

        dropboxService.deleteFile(attachment.getDropboxFileID());

        attachmentRepository.delete(attachment);
    }
}
