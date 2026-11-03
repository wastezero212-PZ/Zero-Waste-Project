package com.example.zerowaste.service;

import com.dropbox.core.v2.files.DeleteResult;
import com.dropbox.core.v2.files.FileMetadata;
import java.io.OutputStream;
import org.springframework.web.multipart.MultipartFile;

public interface DropboxService {
    FileMetadata uploadFile(MultipartFile file);

    FileMetadata fetchFile(String dropboxFileId, OutputStream outputStream);

    DeleteResult deleteFile(String dropboxFileId);
}
