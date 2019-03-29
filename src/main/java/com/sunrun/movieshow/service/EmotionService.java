package com.sunrun.movieshow.service;

import com.sunrun.movieshow.common.CommonUtils;
import com.sunrun.movieshow.common.FileUtils;
import com.sunrun.movieshow.comparator.MyWordComparator;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import scala.Tuple2;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmotionService {
    @Autowired
    private JavaSparkContext sc;

    @Value("${emotion.upload}")
    private String emotionUpload;

    @Value("${emotion.library}")
    private String emotionLibrary;

    public HashMap<String,Object> startEmotion(String inputFile) throws Exception {
        HashMap<String,Object> returnData = new HashMap<>();
        // 1.load emotion library.
        // positive
        JavaPairRDD<String, Integer> positivePairRDD = sc.textFile(emotionLibrary + "ntusd-positive.txt").mapToPair(t -> new Tuple2<>(t, 1));
        // negative
        JavaPairRDD<String, Integer> negativePairRDD = sc.textFile(emotionLibrary + "ntusd-negative.txt").mapToPair(t -> new Tuple2<>(t, 1));

        // 2.load analysis text.
        JavaPairRDD<String, Integer> dataPairRDD = sc.wholeTextFiles(inputFile).map(c -> {
            return ToAnalysis.parse(c._2).getTerms().stream()
                    .filter(t -> {
                        return t.getNatureStr() != "null" && !t.getNatureStr().equals("w");
                    })
                    .map(t -> t.getName())
                    .collect(Collectors.toList());

        }).flatMap(t -> t.iterator()).mapToPair(word -> new Tuple2<>(word, 1));

        // 3.join the p and negative library
        JavaPairRDD<String, Tuple2<Integer, Integer>> joinP = dataPairRDD.join(positivePairRDD);
        JavaPairRDD<String, Tuple2<Integer, Integer>> joinN = dataPairRDD.join(negativePairRDD);
        System.out.println("=== debug:show join result ===");
        System.out.println("=== positive ===");
        returnData.put("positive",joinP.collect());
        System.out.println("=== negative ===");
        returnData.put("negative",joinN.collect());
        returnData.put("result",joinP.count() - joinN.count());
        returnData.put("message","success!");
        System.out.println(returnData);
        return returnData;
    }

    public String upload(MultipartFile file) throws Exception{
        return FileUtils.uploadFile(file, emotionUpload);
    }

}
