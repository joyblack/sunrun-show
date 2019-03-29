package com.sunrun.movieshow.controller;

import com.sunrun.movieshow.service.PagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequestMapping("pager")
@Controller
public class PagerController {
    @Autowired
    private PagerService pagerService;

    @Value("${pager.database}")
    private String database;


    @RequestMapping({"index","/","library"})
    public String library(Model model){
        BufferedReader reader;
        List<HashMap<String,Object>> result = new ArrayList<>();
        try {
           reader = new BufferedReader(new InputStreamReader(new FileInputStream(database)));
           String line = null;
           Integer i = 0;
           while((line = reader.readLine()) != null){
               HashMap<String,Object> data = new HashMap<>();
               String[] split = line.split("\t");
               data.put("id", i++);
               data.put("name", split[0]);
               data.put("uuid", split[1]);
               data.put("time", split[2]);
               result.add(data);
           }
            System.out.println(result);
            model.addAttribute("files", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "/pager/library";
    }

    @RequestMapping("/learn")
    public String learn(){
        return "/pager/learn";
    }


    @RequestMapping("/startLearn")
    public String startLearn(@RequestParam("learnFile")MultipartFile file){
        try{
            String path = pagerService.upload(file,"learn");
            if(path != null){
                pagerService.learnData(path);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "redirect:/pager/learn";
        }
        return "redirect:/pager/library";
    }

    @RequestMapping("/check")
    public String check(){
        return "/pager/check";
    }

    @RequestMapping("/startCheck")
    public String startCheck(@RequestParam("checkFile")MultipartFile file, HttpSession model){
        try{
            String path = pagerService.upload(file,"check");
            if(path == null){
                return "redirect:/pager/check";
            }else{
                HashMap<String, Object> data = pagerService.checkData(path);
                System.out.println(data);
                model.setAttribute("data", data);
                return "redirect:/pager/check-result";
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "redirect:/pager/check";
        }
    }

    @RequestMapping("/check-result")
    public String checkResult(HttpServletRequest request){
        HttpSession session = request.getSession();
        System.out.println(session.getAttribute("data"));
        return "/pager/check-result";
    }


}
