package ma.fellahia.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fellahia.exception.CustomExceptions.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * Uploads a file to MinIO and returns the object key.
     *
     * @param file   the file to upload
     * @param prefix e.g. "cases/uuid-of-case"
     * @return the storage key (object path in MinIO)
     */
    public String upload(MultipartFile file, String prefix) {
        String ext = getExtension(file.getOriginalFilename());
        String key = prefix + "/" + UUID.randomUUID() + ext;
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            log.info("Uploaded file to MinIO: {}", key);
            return key;
        } catch (Exception e) {
            throw new StorageException("فشل رفع الملف: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Generates a pre-signed download URL valid for 1 hour.
     */
    public String generatePresignedUrl(String key) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(key)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            throw new StorageException("فشل توليد رابط التنزيل", e);
        }
    }

    /**
     * Deletes a file from MinIO.
     */
    public void delete(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build());
        } catch (Exception e) {
            log.error("Failed to delete object {}: {}", key, e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
