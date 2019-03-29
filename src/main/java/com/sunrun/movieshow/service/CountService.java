package com.sunrun.movieshow.service;

import com.sunrun.movieshow.common.CommonUtils;
import com.sunrun.movieshow.common.FileUtils;
import com.sunrun.movieshow.comparator.MyWordComparator;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import scala.Tuple2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CountService {
    @Autowired
    private JavaSparkContext sc;

    @Value("${pager.upload.count}")
    private String uploadCount;

    public HashMap<String,Object> count(String inputFile) throws Exception {
        HashMap<String,Object> returnData = new HashMap<>();
        // handler file
        String sparkFile = CommonUtils.handlerFile(inputFile);
        // get all file info
        JavaPairRDD<String, String> checkRDD = sc.wholeTextFiles(sparkFile);

        // 2.analysis data and filter data: null，sig，number(m)
        JavaRDD<String> filterWords = checkRDD.flatMap(s -> BaseAnalysis.parse(s._2).getTerms().iterator())
                .filter(t -> { System.out.println(t.getName() + ":" + t.getNatureStr() + ":" +  (t.getNatureStr() == "null"));

                return t.getName() != "null"
                        && !t.getName().trim().isEmpty()
                        && t.getNatureStr() != "null"
                        && !t.getNatureStr().isEmpty()
                        && !t.getNatureStr().equals("w")
                        && !t.getNatureStr().equals("m");
                }).map(t -> t.getName());
        List<String> filterWordsCollect = filterWords.collect();
        returnData.put("words", filterWordsCollect);
        returnData.put("wordsCount", filterWordsCollect.size());

        // 3.map(word,1)
        JavaPairRDD<String, Integer> wordsCount = filterWords.mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((ctx, cty) -> ctx + cty);

        // 4.show ordered words.
        List<Tuple2<String, Integer>> top10Words = wordsCount.takeOrdered(10, new MyWordComparator());
        System.out.println(top10Words);
        returnData.put("top10Words", top10Words);
        returnData.put("message","success!");
        return returnData;
    }

    public String upload(MultipartFile file) throws Exception{
        return FileUtils.uploadFile(file, uploadCount);
    }

}
