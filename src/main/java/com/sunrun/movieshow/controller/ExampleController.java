package com.sunrun.movieshow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "example")
@Controller
public class ExampleController {
    @RequestMapping("word-count")
    public String wordCount(){
        return "/example/word-count";
    }
}
