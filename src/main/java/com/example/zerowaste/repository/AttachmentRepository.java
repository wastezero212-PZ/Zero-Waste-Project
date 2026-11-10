package com.example.zerowaste.repository;

import com.example.zerowaste.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
