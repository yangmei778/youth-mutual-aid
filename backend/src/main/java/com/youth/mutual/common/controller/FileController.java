package com.youth.mutual.common.controller;

import com.youth.mutual.common.result.R;
import com.youth.mutual.common.utils.MinioUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Tag(name = "文件上传")
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioUtils minioUtils;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public R<String> uploadFile(@RequestParam("file") MultipartFile file,
                                @RequestParam(defaultValue = "default") String directory) {
        String objectName = minioUtils.uploadFile(file, directory);
        String url = minioUtils.getFileUrl(objectName);
        return R.ok(url);
    }
}
