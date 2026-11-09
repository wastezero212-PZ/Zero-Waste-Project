package com.example.zerowaste.service;

import com.example.zerowaste.model.Attachment;
import java.io.OutputStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {
    Attachment save(MultipartFile file);

    Page<Attachment> getAllAttachments(Pageable pageable);

    Attachment getAttachmentById(Long id);

    void deleteById(Long id);

    void streamAttachmentFile(Long id, OutputStream outputStream);
}
