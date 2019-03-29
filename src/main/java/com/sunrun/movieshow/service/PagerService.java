package com.sunrun.movieshow.service;

import com.sunrun.movieshow.common.CommonUtils;
import com.sunrun.movieshow.common.FileUtils;
import com.sunrun.movieshow.comparator.MyWordComparator;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.apache.ivy.util.FileUtil;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import scala.Tuple2;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PagerService {
    @Autowired
    private JavaSparkContext sc;

    @Value("${pager.library}")
    private String pagerLibrary;

    @Value("${pager.document}")
    private String pagerDocument;


    @Value("${pager.database}")
    private String database;

    @Value("${pager.upload.check}")
    private String uploadCheck;

    @Value("${pager.upload.learn}")
    private String uploadLearn;

    public void learnData(String inputFile) throws Exception {
        String sparkFile = CommonUtils.handlerFile(inputFile);
        // 1.load file
        JavaPairRDD<String, String> contentRDD = sc.wholeTextFiles(sparkFile);

        // 2.analysis data and filter data: null，sig，number(m)
        JavaRDD<String> filterWords = contentRDD.flatMap(s -> BaseAnalysis.parse(s._2).getTerms().iterator())
                .filter(t -> t.getName() != null
                        && !t.getName().trim().isEmpty()
                        && !t.getNatureStr().equals("w")
                        && !t.getNatureStr().equals("m")).map(t -> t.getName());

        // 3.map(word,1)
        JavaPairRDD<String, Integer> wordsCount = filterWords.mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((ctx, cty) -> ctx + cty);
        // debug 3
        System.out.println("=== debug3: print the words count===");
        System.out.println(wordsCount.collect());

        // 4.show ordered words.
        System.out.println("=== debug4: print the Top 10 words and appear count===");
        List<Tuple2<String, Integer>> top10Words = wordsCount.takeOrdered(10, new MyWordComparator());
        System.out.println(top10Words);

        // 5.tmp store result
        String newUUID = UUID.randomUUID().toString();
        String nowOutPath = "/output_" + newUUID;
        System.out.println("=== debug5: store the result file to tmp library.===");
        wordsCount.map(s -> s._1 + "\t" + s._2).saveAsTextFile(nowOutPath);
        System.out.println("success...");


        // 6.move to library file.
        System.out.println("=== debug6: move file to learn library vector.===");
        Files.move(Paths.get(nowOutPath + "/" + "part-00000"), Paths.get(pagerLibrary +  newUUID));
        System.out.println("success...");

        // 7.move learn file to library doc.
        System.out.println("=== debug7: move learn file to library doc.===");
        Files.copy(Paths.get(inputFile), Paths.get(pagerDocument + newUUID));
        System.out.println("success...");

        // 8.let library file to database.
        BufferedWriter writer = new BufferedWriter(new FileWriter(database,true));
        writer.write(inputFile + "\t" + newUUID + "\t" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\r\n");
        writer.flush();
        writer.close();



        // 9.print success
        System.out.println("###########################################################");
        System.out.println("##################### Success Learn and Store in Library index:" +  newUUID  + " #################");
        System.out.println("###########################################################");
    }


    public HashMap<String,Object> checkData(String inputFile) throws Exception {
        HashMap<String,Object> returnData = new HashMap<>();
        if(inputFile == null){
            returnData.put("message","error: please upload a file.");
        }else{
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
            System.out.println("=== debug1: print the filter words ===");
            List<String> filterWordsCollect = filterWords.collect();
            System.out.println(filterWordsCollect);
            returnData.put("filterWords", filterWordsCollect);

            // 3.map(word,1)
            JavaPairRDD<String, Integer> wordsCount = filterWords.mapToPair(word -> new Tuple2<>(word, 1))
                    .reduceByKey((ctx, cty) -> ctx + cty);

            // 4.show ordered words.
            System.out.println("=== print the Top 10 words and appear count ===");
            List<Tuple2<String, Integer>> top10Words = wordsCount.takeOrdered(10, new MyWordComparator());
            System.out.println(top10Words);
            returnData.put("top10Words", top10Words);

            // 5.load out SunRun page library
            JavaPairRDD<String, String> libraryRDD = sc.wholeTextFiles(pagerLibrary);
            List<String> libraryFilePaths = libraryRDD.map(l -> l._1).collect();

            // 6.compare with each pager.
            System.out.println("=== Compare document with each pager ===");
            List<HashMap<String, Object>> result = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.00%");
            for(String libraryFilePath:libraryFilePaths){
                HashMap<String, Object> r1 = new HashMap<>();
                JavaRDD<String> nowPageRDD = sc.textFile(libraryFilePath);
                JavaPairRDD<String, Integer> libraryWordsRDD = nowPageRDD.mapToPair(line -> {
                    String[] tokens = line.split("\t");
                    return new Tuple2<>(tokens[0], Integer.valueOf(tokens[1]));
                });
                // join
                JavaPairRDD<String, Tuple2<Integer, Integer>> joinResult = wordsCount.join(libraryWordsRDD);

                // show now check file join the library data info.
                List<Tuple2<String, Tuple2<Integer, Integer>>> intersectionCollect = joinResult.collect();
                System.out.println("=== The join result: ====");
                System.out.println(intersectionCollect);
                r1.put("intersection_words",intersectionCollect);

                // intersection count
                Long intersectionCount = joinResult.count();
                System.out.println("=== The intersection count is: " + intersectionCount + "====");
                r1.put("intersection_words_count",intersectionCount);

                // union
                System.out.println("=== The union result ====");
                JavaPairRDD<String, Integer> unionResult = wordsCount.union(libraryWordsRDD);
                List<Tuple2<String, Integer>> unionCollect = unionResult.collect();
                System.out.println(unionCollect);
                // union count
                long unionCount = unionResult.count() - intersectionCount;
                System.out.println("=== The union count is: " + unionCount + "====");
                r1.put("union_words_count",unionCount);

                // compute jaccard similarity
                double jaccardSimilarity = intersectionCount / (unionCount * 1.0);
                r1.put("file", libraryFilePath);
                r1.put("jaccard", decimalFormat.format(Double.valueOf(jaccardSimilarity)));
                result.add(r1);
            }
            returnData.put("computeResult",result);
            returnData.put("message","success!");

        }
        return returnData;
    }

    public String upload(MultipartFile file, String type) throws Exception{
        return FileUtils.uploadFile(file, type == "check" ? uploadCheck:uploadLearn);
    }

}
