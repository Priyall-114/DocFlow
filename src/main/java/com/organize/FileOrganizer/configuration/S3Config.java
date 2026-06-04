package com.organize.FileOrganizer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    @Value("${aws.accessKeyId}")
    private String acessKeyId;
    @Value("${aws.SecretKey}")
    private String SecretKey;
    @Value("${aws.region}")
    private String region;

    @Bean
    public S3Client s3Client(){
//AwsBasicCredentials — AWS ka ek class jo tumhari Access Key ID aur Secret Key ko ek saath ek object mein wrap karta hai.
//.create() — static factory method hai — directly new nahi karte, .create() se object milta hai.

        AwsBasicCredentials credentials=AwsBasicCredentials.create(acessKeyId,SecretKey);

// S3Client — AWS SDK ka main class jo S3 se baat karta hai. Upload, download, delete — sab iske through hoga.
//.builder() — Builder pattern hai. Matlab S3Client directly nahi banta, pehle ek builder object milta hai jisme step by step configuration add karte hain, phir .build() se final object banta hai.

    return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
 //S3Client ko batao ki authentication ke liye kaunse credentials use karo       .
// StaticCredentialsProvider — "Static" matlab fixed credentials. Har request mein same Access Key aur Secret Key use hogi. Alternative hota hai dynamic credentials — jo automatically rotate hoti hain, but woh advanced use case hai.

            .build();


    }
}
