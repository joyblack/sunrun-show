package com.sunrun.movieshow.controller;

import com.sunrun.movieshow.service.EmotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

@RequestMapping("/emotion")
@Controller
public class EmotionController {

    @Autowired
    private EmotionService emotionService;

    @RequestMapping("/index")
    public String index(){
        return "/emotion/index";
    }

    @RequestMapping("/start")
    public String start(@RequestParam("emotionFile")MultipartFile file, HttpSession model){
        try{
            String path = emotionService.upload(file);
            if(path == null){
                return "redirect:/emotion/index";
            }else{
                HashMap<String, Object> data = emotionService.startEmotion(path);
                model.setAttribute("data", data);
                return "redirect:/emotion/result";
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "redirect:/emotion/index";
        }
    }

    @RequestMapping("/result")
    public String result(HttpServletRequest request){
        HttpSession session = request.getSession();
        return "/emotion/result";
    }


}
