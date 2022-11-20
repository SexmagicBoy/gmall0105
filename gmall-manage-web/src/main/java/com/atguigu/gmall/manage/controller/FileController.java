package com.atguigu.gmall.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@CrossOrigin
public class FileController {

    @ResponseBody
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        // 将图片上传到文件系统

        // 将图片的 URL 返回给页面
        String imgUrl = "https://img13.360buyimg.com/n7/jfs/t4177/323/133450331/58871/8754fee4/58afc7b7Nd5208ecc.jpg";
        return imgUrl;
    }
}
