package com.thm.whatsappclone.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    @Value("${application.file.uploads.media-output-path}")
    private String fileUploadPath;


    public String saveFile(
            @NonNull MultipartFile sourceFile, @NonNull String userId) {
        final String fileUploadSubPath = "users" + separator + userId;
        return uploadFile(sourceFile, fileUploadSubPath);


    }

    private String uploadFile(@NonNull MultipartFile sourceFile, String fileUploadSubPath) {
        final String finalUploadPath = fileUploadPath + separator + fileUploadSubPath;
        File targetFolder = new File(finalUploadPath);

        if(!targetFolder.exists()){
            boolean folderCreated = targetFolder.mkdirs();
            if(!folderCreated){
                log.warn("Failed to create folder {}", targetFolder.getAbsolutePath());
                return null;
            }
        }

        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        String targetFileName =  finalUploadPath + separator + currentTimeMillis() + "." + fileExtension;
        Path targetPath = Paths.get(targetFileName);
        try{
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File uploaded successfully to {}", targetFileName);
            return targetFileName;
        }catch (IOException e){
            log.error("Failed to upload file {}", sourceFile.getOriginalFilename(), e);

        }

        return null;

    }

    private String getFileExtension(String filename) {
        if(filename == null || filename.isEmpty()){
            return "";

        }
        int lastDotIndex = filename.lastIndexOf('.');
        if(lastDotIndex == -1){
            return "";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();


    }
}
