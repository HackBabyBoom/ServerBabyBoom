package com.example.demo.src.externalOpenAPI;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Data;

@Data
public class AWSDao {

    private String accessKey = "AKIARRT7YCIS5H4Y32MI";
    private String secretKey = "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";
    private String bucketName = "babyboom";
    private String folderName = "";
    private AmazonS3 s3Client;

    public AWSDao(){
        AWSCredentials crd = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(crd))
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();
    }

}
