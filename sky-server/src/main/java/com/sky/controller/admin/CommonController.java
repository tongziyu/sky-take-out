package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/6 14:26
 */
@RestController
@Slf4j
@Api(tags = "公共接口")
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;


    @PostMapping("/admin/common/upload")
    @ApiOperation("上传文件")
    public Result<String> upload(MultipartFile file){
        log.info("文件的名字:" + file.getOriginalFilename());

        // 获取拓展名
        String originalFilename = file.getOriginalFilename();

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String name = UUID.randomUUID() + extension;

        // 返回值:返回的图片的外网访问路径
        String url;
        try {

            url = aliOssUtil.upload(file.getBytes(),name);
            log.info("存往OSS服务器的文件名字:{}",name);
            log.info("上传文件的外网访问路径:{}" ,url);
            return Result.success(url);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 测试文件上传
     *  - 前端表单的name必须和形参的名字一致,如果不一致就使用@RequestParam 注解 指定name
     *  - 使用transferTo() 可以将文件保存到本地
     *  - 文件夹的名字,最好使用UUID生成,然后拼接上文件的后缀
     * @param file
     * @return
     */
    @PostMapping("/upload/test")

    public String upload1(MultipartFile file){
        log.info("文件的名字:" + file.getOriginalFilename());


        try {
            // 拼接文件后缀
            String originalFilename = file.getOriginalFilename();

            int i = originalFilename.lastIndexOf(".");

            String substring = originalFilename.substring(i);

            UUID uuid = UUID.randomUUID();

            String fileName = uuid.toString() + substring;

            file.transferTo(new File("/Users/tongziyu/test/" + fileName));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "ok!";

    }
    @PostMapping("/uploadOSS/test")
    public String uploadOss(MultipartFile file){
        log.info("文件的名字:" + file.getOriginalFilename());

        // 获取拓展名
        String originalFilename = file.getOriginalFilename();

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String name = UUID.randomUUID() + extension;

        // 返回值:返回的图片的外网访问路径
        String upload;
        try {


            upload = aliOssUtil.upload(file.getBytes(),name);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return upload;

    }


}
