package com.example.computershop.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);
    private final Path location;
    
    public FileSystemStorageService() {
        this.location = Paths.get("src/main/resources/static/assets/images/product/laptop");
    }
    
    @Override
    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
            
            // Tạo tên file unique để tránh conflict
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            Path destinationFile = this.location.resolve(Paths.get(uniqueFilename)).normalize().toAbsolutePath();
            
            // Kiểm tra path để tránh directory traversal attack
            if (!destinationFile.getParent().equals(this.location.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }
            
            try (InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("File stored successfully: {} -> {}", originalFilename, uniqueFilename);
            return uniqueFilename;
        } catch (IOException e) {
            logger.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store file.", e);
        }
    }
    
    @Override
    public void init() {
        try {
            Files.createDirectories(location);
            logger.info("Storage directory initialized: {}", location);
        } catch (IOException e) {
            logger.error("Failed to initialize storage directory: {}", location, e);
            throw new RuntimeException("Could not initialize storage", e);
        }
    }
    
    @Override
    public void delete(String fileName) {
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                return;
            }
            Path filePath = this.location.resolve(fileName).normalize().toAbsolutePath();
            
            // Kiểm tra path để tránh xóa file ngoài thư mục
            if (!filePath.getParent().equals(this.location.toAbsolutePath())) {
                logger.warn("Attempted to delete file outside storage directory: {}", fileName);
                return;
            }
            
            Files.deleteIfExists(filePath);
            logger.info("File deleted successfully: {}", fileName);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", fileName, e);
        }
    }
    
    // Phương thức mới để store và cleanup file cũ
    public String storeAndCleanup(MultipartFile newFile, String oldFileName) {
        String newFileName = store(newFile);
        if (oldFileName != null && !oldFileName.trim().isEmpty()) {
            delete(oldFileName);
        }
        return newFileName;
    }
}
