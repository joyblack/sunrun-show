package com.sunrun.movieshow.common;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

public class FileUtils {
    public static int getFileType(String filePath){
        String[] split = filePath.split("\\.");
        String suffix = split[split.length -1];
        if(suffix.equals("doc")){
            return 1;// doc
        }else if(suffix.equals("docx")){
            return 2; // docx
        }else{
            return 0;
        }
    }

    public static String uploadFile(MultipartFile file, String uploadPath) throws Exception{
        String absolutePath;
        File targetFile = new File(uploadPath);
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        FileOutputStream out = null;
        try {
            absolutePath = uploadPath + file.getOriginalFilename();
            out = new FileOutputStream(absolutePath);
            out.write(file.getBytes());

        } catch (Exception e) {
            absolutePath = null;
            e.printStackTrace();
        }finally {
            out.flush();
            out.close();
        }
        return absolutePath;
    }
}
