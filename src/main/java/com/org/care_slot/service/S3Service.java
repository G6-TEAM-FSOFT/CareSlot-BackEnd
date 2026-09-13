package com.org.care_slot.service;

import com.org.care_slot.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    /**
     * Upload an image file to AWS S3.
     *
     * @param file   the multipart file from HTTP request
     * @param folder folder prefix on S3 (e.g. "doctors", "clinics")
     * @return FileUploadResponse containing the public S3 URL and metadata
     */
    FileUploadResponse uploadImage(MultipartFile file, String folder);

    /**
     * Delete an object on S3 by full URL or S3 key.
     *
     * @param fileUrlOrKey S3 URL or object key
     */
    void deleteFile(String fileUrlOrKey);

    /**
     * Extract S3 object key from a full S3 URL.
     *
     * @param fileUrl full URL like https://bucket.s3.region.amazonaws.com/folder/key
     * @return object key like folder/key
     */
    String extractKeyFromUrl(String fileUrl);
}
