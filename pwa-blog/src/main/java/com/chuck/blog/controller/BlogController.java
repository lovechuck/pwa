package com.chuck.blog.controller;


import com.chuck.blog.entity.Article;
import com.chuck.blog.repository.ArticleRepository;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@RestController
public class BlogController {

    @Autowired
    private ArticleRepository repository;

    @RequestMapping(value = "publish",method = RequestMethod.POST)
    public int publish(@RequestBody Article article){
        article.setGuid(UUID.randomUUID().toString());
        article.setDate(new Date());
        repository.insert(article);
        return 1;
    }

    @RequestMapping(value = "remove",method = RequestMethod.GET)
    public void remove(String guid, HttpServletResponse response) throws IOException {
        repository.delete(repository.findByGuid(guid));
        response.sendRedirect("");
    }

    @RequestMapping(value = "create",method = RequestMethod.GET)
    public ModelAndView create(){
        return new ModelAndView("create");
    }

    @RequestMapping(value = "/",method = RequestMethod.GET)
    public ModelAndView index(){
        List<Article> items = repository.findAll();
        Map<String,List<Article>> map = new HashMap<>();
        map.put("items",items);
        ModelAndView vm = new ModelAndView("",map);
        return vm;
    }

}
