package com.sunrun.movieshow.controller;

import com.sunrun.movieshow.bean.KNNData;
import com.sunrun.movieshow.service.KNNService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@RequestMapping("knn")
@Controller
public class KNNController {

    @Autowired
    private KNNService knnService;

    @RequestMapping("alg")
    public String alg(){
        return "knn/alg";
    }

    @ResponseBody
    @RequestMapping("algStart")
    public String algStart(@RequestBody KNNData knnData){

        return "knn/alg";
    }

    // data/knn/...
    @ResponseBody
    @RequestMapping("getData")
    public HashMap<String, Object> getKNNData(){
        return knnService.getData();
    }
}
