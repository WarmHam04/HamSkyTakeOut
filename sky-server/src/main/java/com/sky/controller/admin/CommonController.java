package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {
    private final AliOssUtil aliOssUtil;

    public CommonController(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
    }

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传，{}", file);
        //获取文件原始名
        String fileName = file.getOriginalFilename();
        //截取后缀，扩展名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //使用uuid生成新的名字
        String newFileName = UUID.randomUUID().toString() + suffixName;

        //构建文件的请求路径
        try {
            String filePath = aliOssUtil.upload(file.getBytes(),newFileName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败,{}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);

    }
}
