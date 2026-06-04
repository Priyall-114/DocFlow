package com.organize.FileOrganizer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

@Service
@Slf4j
public class s3service {
    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    //Uploading file
    public String uploadFile(String fileName, InputStream fileStream,long fileSize,String contentType){
        //fileName mera iss type ka aayegaa String s3Key = username + "/" + folderName + "/" + originalFileName;
        PutObjectRequest putObjectRequest=PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();

        //actual data upload
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fileStream,fileSize));

        //returning address of uploaded file in path taaki metadata mein path bhi daal ske
        return "https://" + bucketName + ".s3.ap-south-1.amazonaws.com/" + fileName;

    }

    // File delete karo S3 se
    public void deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                // kaunsi file delete karni hai
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        // actual delete ho raha hai yahan
    }

    //Copying file
    public void copyFile(String oldS3key ,String newS3Key){
        CopyObjectRequest copyObjectRequest= CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(oldS3key)
                .destinationBucket(bucketName)
                .destinationKey(newS3Key)
                .build();
        s3Client.copyObject(copyObjectRequest);
    }

}
