package com.liu.picturebackend.controller;

import cn.hutool.http.server.HttpServerResponse;
import com.liu.picturebackend.annotation.AuthCheck;
import com.liu.picturebackend.common.BaseResponse;
import com.liu.picturebackend.common.ResultUtils;
import com.liu.picturebackend.constant.UserConstant;
import com.liu.picturebackend.manager.CosManager;
import com.liu.picturebackend.model.enums.UserRoleEnum;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 测试文件上传
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/fileUpload")
    public BaseResponse<?> fileUpload(@RequestPart("file")MultipartFile multipartFile) {
        //文件目录
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s",fileName);


        File file = null;
        try {
            //创建空临时文件进行接受
            file = File.createTempFile(filePath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath,file);
        } catch (Exception e) {
            log.error("file upload error filepath = "+ filePath,e);
            throw new RuntimeException(e);
        }finally {
            if (file != null){
                boolean delete = file.delete();
                if (!delete){
                    log.error("file delete error filepath = {}",filePath);
                }
            }
        }

        return ResultUtils.success(filePath);
    }

    /**
     * 测试下载
     * @param filepath
     * @param response
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/downLoad")
    public void downLoad(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream content = null;
        try {
            COSObject object = cosManager.getObject(filepath);
            content = object.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(content);
            log.info("当前下载的文件真实大小: {} 字节", byteArray.length);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            //写入
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("file download error filepath = "+ filepath,e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            //释放
            if (content !=null){
                content.close();
            }
        }

    }

}
