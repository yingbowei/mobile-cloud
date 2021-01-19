package com.wmeimob.fastboot.mobile.sdk;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.*;
import java.util.UUID;

/**
 * @author bowei.ying
 * @description 测试
 * @date 2021/1/14
 */
public class Test {
    private static final String accessKeyId = "HABHJ3JZHTJ1BBKVWY96";
    private static final String accessSecretKey = "QUrKi4vz4sEnIIYJ3IwKF0AuwDNLOZOqqaatt11h";
    private static final String hostname = "cmecloud.cn";
    private static final String bucket = "dangyuanxiechen-2021";
    private static final String region = "eos-shanghai-1";

    public static void main(String[] args) {
        ClientConfiguration opts = new ClientConfiguration();
        opts.setSignerOverride("S3SignerType");
        AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, accessSecretKey);
        AmazonS3Client client = new AmazonS3Client(credentials, opts);
        String endPoint = region + "." + hostname;
        client.setEndpoint(endPoint);
        File  file = new File("/Users/yingbowei/Downloads/test.jpg");
        String fileName = file.getName();
        String key = UUID.randomUUID().toString() + fileName.substring(fileName.indexOf("."), fileName.length());
        // 上传
        InputStream content = null;
        try {
            content = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PutObjectRequest request = new PutObjectRequest(bucket, key, content,
                null);
        request.setCannedAcl(CannedAccessControlList.PublicReadWrite); //设置ACL
        PutObjectResult putObjectResult = client.putObject(request);

//        String key = UUID.randomUUID().toString() + ;
//        PutObjectResult putObjectResult = client.putObject(bucket, "test.jpg", inputStream, null);
//        putObjectResult.

        client.shutdown();
        String imgPath = "http://" + region + ".cmecloud.cn/" + bucket + "/" + key;
        System.out.println(imgPath);
    }
}
