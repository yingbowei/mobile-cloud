//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.wmeimob.fastboot.mobile.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties("fastboot.mobile.eos")
public class EosProperties {
    private String accessKeyId;
    private String accessSecretKey;
    private String bucket;
    private String region;
    private Integer expire;
    private Boolean secure = Boolean.FALSE;
}
