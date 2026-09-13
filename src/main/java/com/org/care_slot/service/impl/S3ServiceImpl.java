package com.org.care_slot.service.impl;

import com.org.care_slot.dto.response.FileUploadResponse;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "webp", "gif"
    );

    @Override
    public FileUploadResponse uploadImage(MultipartFile file, String folder) {
        validateImageFile(file);

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        String extension = getFileExtension(originalFilename).toLowerCase();

        String cleanFolder = (folder == null || folder.trim().isEmpty()) ? "uploads" : folder.trim().replaceAll("^/+|/+$", "");
        String s3Key = cleanFolder + "/" + UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            contentType = "image/" + (extension.equals("jpg") ? "jpeg" : extension);
        }

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
            s3Client.putObject(putRequest, requestBody);

            String publicUrl = buildPublicUrl(s3Key);
            log.info("Successfully uploaded image to S3: {}", publicUrl);

            return FileUploadResponse.builder()
                    .url(publicUrl)
                    .fileName(originalFilename)
                    .fileSize(file.getSize())
                    .contentType(contentType)
                    .build();

        } catch (S3Exception e) {
            log.error("S3 error while uploading file: HTTP {} - {}", e.statusCode(), e.awsErrorDetails().errorMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (IOException e) {
            log.error("IO error while reading multipart file: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteFile(String fileUrlOrKey) {
        if (fileUrlOrKey == null || fileUrlOrKey.trim().isEmpty()) {
            return;
        }

        String key = extractKeyFromUrl(fileUrlOrKey);
        if (key == null || key.trim().isEmpty()) {
            return;
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted S3 object with key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to delete file from S3 (key: {}): {}", key, e.getMessage());
        }
    }

    @Override
    public String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return null;
        }

        String trimmed = fileUrl.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return trimmed;
        }

        try {
            URI uri = URI.create(trimmed);
            String path = uri.getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (Exception e) {
            log.warn("Could not parse S3 URL: {}", fileUrl);
            return null;
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String extension = getFileExtension(originalFilename).toLowerCase();

        boolean validContentType = contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
        boolean validExtension = ALLOWED_EXTENSIONS.contains(extension);

        if (!validContentType && !validExtension) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    private String buildPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}
