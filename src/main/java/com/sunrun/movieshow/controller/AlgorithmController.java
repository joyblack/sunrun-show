package com.sunrun.movieshow.controller;

import com.sunrun.movieshow.bean.SetData;
import com.sunrun.movieshow.service.SimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
@RequestMapping("/algorithm")
public class AlgorithmController {
    @Autowired
    private SimilarityService similarityService;

    @RequestMapping("jaccard")
    public String jaccard(){
        return "/algorithm/jaccard";
    }

    @RequestMapping("computeJaccard")
    @ResponseBody
    public HashMap<String, Object> computeJaccard(@RequestBody SetData data){
        return similarityService.jaccard(data.getA(),data.getB(),",");
    }

    @RequestMapping("cosine")
    public String cosine(){
        return "/algorithm/cosine";
    }

    @RequestMapping("computeCosine")
    @ResponseBody
    public HashMap<String, Object> computeCosine(@RequestBody SetData data){
        return similarityService.cosine(data.getA(),data.getB(),",");
    }

    @RequestMapping("pearson")
    public String pearson(){
        return "/algorithm/pearson";
    }

    @RequestMapping("computePearson")
    @ResponseBody
    public HashMap<String, Object> computePearson(@RequestBody SetData data){
        return similarityService.pearson(data.getA(),data.getB(),",");
    }
}
