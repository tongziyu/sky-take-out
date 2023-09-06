package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/6 16:08
 */
@Configuration
@Slf4j
public class OssConfiguration {

    /**
     *  阿里云OSS对象存储工具类
     * @ConditionalOnMissingBean: 如果容器里面没有就创建这个bean
     * @param aliOssProperties
     * @return
     */
    @ConditionalOnMissingBean
    @Bean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        return new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName()
        );
    }
}
