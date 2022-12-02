package com.atguigu.gmall.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import util.UploadUtil;

@Controller
@CrossOrigin
public class FileController {

    @ResponseBody
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        // 将图片上传到文件系统，将图片的 URL 返回给页面
        return UploadUtil.uploadImage(multipartFile);
    }
}
