package com.sunrun.movieshow.controller;

import com.sunrun.movieshow.bean.NLPData;
import com.sunrun.movieshow.service.HanNLPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
@RequestMapping("nlp")
public class NLPController {

    @Autowired
    private HanNLPService hanNLPService;


    @RequestMapping({"/","segment"})
    public String segment(){
        return "nlp/segment";
    }

    @RequestMapping("startSeg")
    @ResponseBody
    public HashMap<String, Object> start(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.seg(data);

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping({"name"})
    public String name(){
        return "nlp/name";
    }

    @RequestMapping("startName")
    @ResponseBody
    public HashMap<String, Object> startName(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.name(data);

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping({"place"})
    public String place(){
        return "nlp/place";
    }

    @RequestMapping("startPlace")
    @ResponseBody
    public HashMap<String, Object> startPlace(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.place(data);

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping({"org"})
    public String org(){
        return "nlp/org";
    }

    @RequestMapping("startOrg")
    @ResponseBody
    public HashMap<String, Object> startOrg(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.organization(data.getContent());

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping({"keyword"})
    public String keyword(){
        return "nlp/keyword";
    }

    @RequestMapping("startKeyword")
    @ResponseBody
    public HashMap<String, Object> startKeyword(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.keyword(data.getContent(),data.getSize());

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping({"summary"})
    public String summary(){
        return "nlp/summary";
    }

    @RequestMapping("startSummary")
    @ResponseBody
    public HashMap<String, Object> startSummary(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.summary(data.getContent(),data.getSize());

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping({"phrase"})
    public String phrase(){
        return "nlp/phrase";
    }

    @RequestMapping("startPhrase")
    @ResponseBody
    public HashMap<String, Object> startPhrase(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.phrase(data.getContent(),data.getSize());

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping({"pin"})
    public String pin(){
        return "nlp/pin";
    }

    @RequestMapping("startPin")
    @ResponseBody
    public HashMap<String, Object> startPin(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.pin(data.getContent());

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping("ts")
    public String ts(){
        return "nlp/ts";
    }

    @RequestMapping("startTS")
    @ResponseBody
    public HashMap<String, Object> startTS(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.t2s(data);

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping("recommend")
    public String recommend(){
        return "nlp/recommend";
    }

    @RequestMapping("startRecommend")
    @ResponseBody
    public HashMap<String, Object> startRecommend(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.recommend(data);

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }

    @RequestMapping("dp")
    public String dp(){
        return "nlp/dp";
    }

    @RequestMapping("startDp")
    @ResponseBody
    public HashMap<String, Object> startDp(@RequestBody NLPData data){
        long start = System.nanoTime();
        String segResult = hanNLPService.dependencyParser(data);

        HashMap<String,Object> result = new HashMap();
        result.put("result",segResult);
        result.put("usedTime", String.format("%.2f",(System.nanoTime() - start)/1000.0/1000.0));
        return result;
    }




}
