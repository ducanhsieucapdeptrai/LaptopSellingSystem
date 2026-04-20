package com.example.computershop.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface StorageService {
    void init();
    String store(MultipartFile file);
    void delete(String fileName);
    
    // Phương thức mới để store file mới và xóa file cũ
    default String storeAndCleanup(MultipartFile newFile, String oldFileName) {
        String newFileName = store(newFile);
        if (oldFileName != null && !oldFileName.trim().isEmpty()) {
            delete(oldFileName);
        }
        return newFileName;
    }
}
