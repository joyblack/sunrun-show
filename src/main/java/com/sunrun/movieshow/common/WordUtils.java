package com.sunrun.movieshow.common;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.FileInputStream;

public class WordUtils {

    public static String getWordDocxText(String filePath) throws Exception {
        String content;
        XWPFWordExtractor extractor = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);

            extractor = new XWPFWordExtractor(new XWPFDocument(fileInputStream));
            content = extractor.getText();

        } catch (Exception e1) {
            System.out.println(e1.getMessage());
            throw new Exception("Handler docx file error:" + e1.getMessage());
        }finally {
            extractor.close();
        }
        return  content;
    }


    public static String getWordDocText(String filePath) throws Exception {
        String content = "";
        HWPFDocument doc = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            doc = new HWPFDocument(fileInputStream);
            content = doc.getText().toString();
            doc.close();
        } catch (Exception e1) {
            System.out.println(e1.getMessage());
            throw new Exception("Handler doc file error... ");
        }finally {
            doc.close();
        }
        return  content;
    }
}
