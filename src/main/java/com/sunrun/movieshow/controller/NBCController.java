package com.sunrun.movieshow.controller;

import com.sunrun.movieshow.bean.KNNData;
import com.sunrun.movieshow.bean.NBCBasketData;
import com.sunrun.movieshow.service.KNNService;
import com.sunrun.movieshow.service.NBCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@RequestMapping("nbc")
@Controller
public class NBCController {

    @Autowired
    private NBCService nbcService;

    @RequestMapping("alg")
    public String alg(){
        return "nbc/alg";
    }

    @ResponseBody
    @RequestMapping("startAlg")
    public HashMap<String,Object> startAlg(@RequestBody NBCBasketData data){
        return nbcService.basket(data);
    }

}
