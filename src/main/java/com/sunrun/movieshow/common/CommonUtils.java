package com.sunrun.movieshow.common;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class CommonUtils {

    // Transform word file to simple, general a file that name is old.uuid
    public static String handlerFile(String url) throws Exception{
        String sparkFile = url;
        int type = FileUtils.getFileType(url);
        BufferedWriter writer = null;
        if(type != 0) {//doc or docx
            String content = null;
            if (type == 1) {
                System.out.println("=== This is a doc file, need nio to handler and transfer...===");
                content = WordUtils.getWordDocText(url);
            } else {
                System.out.println("=== This is a docx file, need nio to handler and transfer...===");
                content = WordUtils.getWordDocxText(url);
            }
            try{
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sparkFile), "UTF-8"));
                writer.write(content);
                writer.flush();
            }catch (Exception e){
                System.out.println(e.getMessage());
                throw new Exception("When write doc data in common file error:" + e.getMessage());
            }finally {
                writer.close();
            }
        }else{
            System.out.println("This is a normal text file, do nothing.");
        }
        return sparkFile;
    }

}
