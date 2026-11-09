package com.example.zerowaste.service.impl;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.example.zerowaste.exception.DropboxException;
import com.example.zerowaste.service.DropboxService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DropboxServiceImpl implements DropboxService {

    @Value("${dropbox.access-token}")
    private String dropboxAccessToken;

    private DbxClientV2 dropboxClient;

    @PostConstruct
    public void init() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("spring-boot-dropbox-app").build();
        this.dropboxClient = new DbxClientV2(config, dropboxAccessToken);
    }

    @Override
    public FileMetadata uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String dropboxPath = "/attachments/" + System.currentTimeMillis() + "_"
                + (originalFilename != null ? originalFilename : "unknown");

        try (InputStream in = file.getInputStream()) {
            return dropboxClient.files().uploadBuilder(dropboxPath)
                    .uploadAndFinish(in);
        } catch (DbxException e) {
            throw new DropboxException("Failed to upload file to Dropbox: " + e.getMessage());
        } catch (IOException e) {
            throw new DropboxException("Failed to read uploaded file input stream");
        } catch (Exception e) {
            throw new DropboxException("An unexpected error occurred during file upload");
        }
    }

    @Override
    public FileMetadata fetchFile(String dropboxFileId, OutputStream outputStream) {
        try {
            return dropboxClient.files().download(dropboxFileId)
                    .download(outputStream);
        } catch (DbxException e) {
            throw new DropboxException("Failed to download file from Dropbox: " + e.getMessage());
        } catch (IOException e) {
            throw new DropboxException("Failed to write file content to output stream");
        } catch (Exception e) {
            throw new DropboxException("An unexpected error occurred during file download");
        }
    }

    @Override
    public DeleteResult deleteFile(String dropboxFileId) {
        try {
            return dropboxClient.files().deleteV2(dropboxFileId);
        } catch (DbxException e) {
            throw new DropboxException("Failed to delete file from Dropbox: " + e.getMessage());
        } catch (Exception e) {
            throw new DropboxException("An unexpected error occurred during file deletion");
        }
    }
}
