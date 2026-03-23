package com.liaw.dev.GraoMestre.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${image.upload.dir:uploads/images}")
    private String uploadDir;

    public String storeImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Falha ao armazenar arquivo vazio.");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;

        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    public boolean deleteImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Erro ao excluir imagem: " + fileName + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Retorna o caminho completo da imagem no sistema de arquivos.
     * Útil para construir URLs ou para acesso direto.
     *
     * @param fileName O nome do arquivo da imagem.
     * @return O caminho completo da imagem.
     */
    public Path loadImagePath(String fileName) {
        return Paths.get(uploadDir).resolve(fileName).normalize();
    }
}