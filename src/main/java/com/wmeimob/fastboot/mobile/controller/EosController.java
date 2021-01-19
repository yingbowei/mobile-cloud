package com.wmeimob.fastboot.mobile.controller;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import com.wmeimob.fastboot.mobile.service.EosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.ws.WebServiceException;
import java.io.*;
import java.util.UUID;

/**
 * @author bowei.ying
 * @description 移动云对象存储
 * @date 2021/1/14
 */
@Slf4j
@RestController
@RequestMapping({"/mobile"})
public class EosController {

    @Autowired
    private EosService eosService;

    @GetMapping({"/eos-url"})
    public String upload(String url) {
        return eosService.fetchStaticsToEos(url);
    }

    @PostMapping({"/eos-upload"})
    public String fileUpload(@RequestBody MultipartFile file) {
        return eosService.upload(file);
    }

    @PostMapping({"/eos-part-upload"})
    public String multipartFileUpload(@RequestBody MultipartFile file) throws Exception {
        try {
            return eosService.multipartFileUpload(file);
        } catch (Exception var3) {
            log.warn(var3.getMessage(), var3);
            throw new Exception("上传失败");
        }
    }
}
