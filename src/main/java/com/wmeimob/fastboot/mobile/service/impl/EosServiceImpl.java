package com.wmeimob.fastboot.mobile.service.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.wmeimob.fastboot.mobile.config.EosProperties;
import com.wmeimob.fastboot.mobile.service.EosService;
import com.wmeimob.fastboot.util.FileFetchUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.util.Assert;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author bowei.ying
 * @description 移动云对象存储
 * @date 2021/1/14
 */
@Service
@Slf4j
public class EosServiceImpl implements EosService {
    private ThreadPoolExecutor threadPoolExecutor;

    public EosServiceImpl() {
        this.threadPoolExecutor = new ThreadPoolExecutor(0, 2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    @Autowired
    private EosProperties eosProperties;


    @Override
    public String fetchStaticsToEos(String url) {
        Assert.notNull(url, "文件资源路径不能为空");
        String prefix = url.substring(url.lastIndexOf("."), url.contains("?") ? url.indexOf("?") : url.length());
        File file = FileFetchUtil.fetchFile(url);
        String key = UUID.randomUUID().toString() + prefix;
        ClientConfiguration opts = new ClientConfiguration();
        opts.setSignerOverride("S3SignerType");
        AWSCredentials credentials = new BasicAWSCredentials(eosProperties.getAccessKeyId(), eosProperties.getAccessSecretKey());
        AmazonS3Client client = new AmazonS3Client(credentials, opts);
        String endPoint = eosProperties.getRegion() + ".cmecloud.cn";
        client.setEndpoint(endPoint);

        // 上传文件流
        InputStream content = null;
        try {
            content = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PutObjectRequest request = new PutObjectRequest(eosProperties.getBucket(), key, content,
                null);
        request.setCannedAcl(CannedAccessControlList.PublicReadWrite); //设置ACL
        client.putObject(request);
        client.shutdown();
        file.delete();

        String imgPath = eosProperties.getRegion() + ".cmecloud.cn/" + eosProperties.getBucket() + "/" + key;
        Boolean secure = eosProperties.getSecure();
        if (secure) {
            imgPath = "https://" + imgPath;
        } else {
            imgPath = "http://" + imgPath;
        }
        return imgPath;
    }

    @Override
    public String upload(MultipartFile file) {
        Assert.notNull(file, "文件不能为空");
        String fileName = file.getOriginalFilename();
        String key = UUID.randomUUID().toString() + fileName.substring(fileName.indexOf("."), fileName.length());
        ClientConfiguration opts = new ClientConfiguration();
        opts.setSignerOverride("S3SignerType");
        AWSCredentials credentials = new BasicAWSCredentials(eosProperties.getAccessKeyId(), eosProperties.getAccessSecretKey());
        AmazonS3Client client = new AmazonS3Client(credentials, opts);
        String endPoint = eosProperties.getRegion() + ".cmecloud.cn";
        client.setEndpoint(endPoint);

        byte[] content = new byte[0];
        try {
            content = file.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PutObjectRequest request = new PutObjectRequest(eosProperties.getBucket(), key, new ByteArrayInputStream(content),
                null);
        request.setCannedAcl(CannedAccessControlList.PublicReadWrite); //设置ACL
        client.putObject(request);
        client.shutdown();
        String imgPath = eosProperties.getRegion() + ".cmecloud.cn/" + eosProperties.getBucket() + "/" + key;
        Boolean secure = eosProperties.getSecure();
        if (secure) {
            imgPath = "https://" + imgPath;
        } else {
            imgPath = "http://" + imgPath;
        }
        return imgPath;
    }

    @Override
    public String multipartFileUpload(MultipartFile file) throws Exception {
        String bucketName = eosProperties.getBucket();
        String tmpSecretId = eosProperties.getAccessKeyId();
        String tmpSecretKey = eosProperties.getAccessSecretKey();
        ClientConfiguration opts = new ClientConfiguration();
        opts.setSignerOverride("S3SignerType");
        AWSCredentials credentials = new BasicAWSCredentials(tmpSecretId, tmpSecretKey);
        AmazonS3Client client = new AmazonS3Client(credentials, opts);
        String endPoint = eosProperties.getRegion() + ".cmecloud.cn";
        client.setEndpoint(endPoint);
        // 设置eos中存储的文件名
        String fileName = file.getOriginalFilename();
        String key = UUID.randomUUID().toString() + fileName.substring(fileName.indexOf("."), fileName.length());
        //指定容器与对象名，也可以在request中同时设置metadata，acl等对象的属性
        InitiateMultipartUploadRequest initUploadRequest = new
                InitiateMultipartUploadRequest(bucketName, key);
        initUploadRequest.setCannedACL(CannedAccessControlList.PublicReadWrite);
        InitiateMultipartUploadResult initResponse =
                client.initiateMultipartUpload(initUploadRequest);
        //用于区分分片上传的唯一标识，后续的操作中会使用该id
        String uploadId = initResponse.getUploadId();
        List<Future<UploadPartResult>> list = new ArrayList();
        long fileSize = file.getSize();
        long partSize = 52428800L;
        int partCount = (int) (fileSize / partSize);
        if (fileSize % partSize != 0L) {
            ++partCount;
        }

        for (int i = 1; i <= partCount; ++i) {
            InputStream inputStream = file.getInputStream();
            long startPos = (long) (i - 1) * partSize;
            long curPartSize = i == partCount ? fileSize - startPos : partSize;
            inputStream.skip(startPos);
            int finalI = i;
            Future<UploadPartResult> submit = this.threadPoolExecutor.submit(() -> {
                UploadPartRequest uploadRequest = (new UploadPartRequest()).withBucketName(bucketName).withUploadId(uploadId).withKey(key).withPartNumber(finalI).withInputStream(inputStream).withPartSize(curPartSize);
                UploadPartResult uploadPartResult = client.uploadPart(uploadRequest);
                return uploadPartResult;
            });
            list.add(submit);
        }

        this.threadPoolExecutor.submit(() -> {
            List<PartETag> partETags = new ArrayList();
            Iterator var6 = list.iterator();

            while (var6.hasNext()) {
                Future future = (Future) var6.next();

                try {
                    partETags.add(((UploadPartResult) future.get()).getPartETag());
                } catch (InterruptedException var9) {
                    var9.printStackTrace();
                } catch (ExecutionException var10) {
                    var10.printStackTrace();
                }
            }

            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, key, uploadId, partETags);
            client.completeMultipartUpload(compRequest);
        });
        String imgPath = eosProperties.getRegion() + ".cmecloud.cn/" + eosProperties.getBucket() + "/" + key;
        Boolean secure = eosProperties.getSecure();
        if (secure) {
            imgPath = "https://" + imgPath;
        } else {
            imgPath = "http://" + imgPath;
        }
        return imgPath;
    }

}
