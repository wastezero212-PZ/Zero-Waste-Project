package com.example.zerowaste.service;

import com.dropbox.core.v2.files.FileMetadata;
import java.io.OutputStream;
import org.springframework.web.multipart.MultipartFile;

public interface DropboxService {
    FileMetadata uploadFile(MultipartFile file);

    void fetchFile(String dropboxFileId, OutputStream outputStream);

    void deleteFile(String dropboxFileId);
}
