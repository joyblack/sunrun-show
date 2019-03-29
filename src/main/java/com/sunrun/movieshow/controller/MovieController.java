package com.sunrun.movieshow.controller;

import com.sunrun.movieshow.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import scala.Tuple2;
import scala.Tuple3;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RequestMapping("/movie")
@Controller
public class MovieController {

    @Autowired
    private MovieService movieService;

    @RequestMapping("/index")
    public String learn(){
        return "/movie/index";
    }

    @RequestMapping("/startMovie")
    public String startMovie(@RequestParam("movieFile")MultipartFile file, HttpSession model){
        try{
            String path = movieService.upload(file);
            if(path == null){
                return "redirect:/movie/index";
            }else{
                Map<Tuple2<String, String>, Tuple3<Double, Double, Double>> data = movieService.startCompute(path);
                model.setAttribute("data", data);
                return "redirect:/movie/result";
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "redirect:/movie/index";
        }
    }

    @RequestMapping("/result")
    public String result(HttpServletRequest request){
        HttpSession session = request.getSession();
        return "/movie/result";
    }


}
