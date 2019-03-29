package com.sunrun.movieshow.controller;

import com.sunrun.movieshow.service.CountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

@RequestMapping("/count")
@Controller
public class CountController {

    @Autowired
    private CountService countService;

    @RequestMapping("/index")
    public String learn(){
        return "/count/index";
    }

    @RequestMapping("/startCount")
    public String startCount(@RequestParam("countFile")MultipartFile file, HttpSession model){
        try{
            String path = countService.upload(file);
            if(path == null){
                return "redirect:/count/index";
            }else{
                HashMap<String, Object> data = countService.count(path);
                model.setAttribute("data", data);
                return "redirect:/count/result";
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "redirect:/count/index";
        }
    }

    @RequestMapping("/result")
    public String checkResult(HttpServletRequest request){
        HttpSession session = request.getSession();
        return "/count/result";
    }


}
