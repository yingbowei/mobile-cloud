package com.wmeimob.fastboot.mobile.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Will Ying
 * @description 移动云对象存储
 * @date 2021/1/14
 */
public interface EosService {

    /**
     * 功能描述: 网络资源上传
     * @Author: bowei.ying
     * @Date: 2021/1/19
     * @param url
     * @return: java.lang.String
     */
    String fetchStaticsToEos(String url);

    /**
     * 功能描述: 文件上传
     * @Author: bowei.ying
     * @Date: 2021/1/18
     * @param file
     * @return: java.lang.String
     */
    String upload(MultipartFile file);


    String multipartFileUpload(MultipartFile file) throws Exception;
}
