package com.organize.FileOrganizer.controller;

import com.organize.FileOrganizer.Repository.UserRepository;
import com.organize.FileOrganizer.entity.Question;
import com.organize.FileOrganizer.entity.user;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;


@RestController

public class FileQueryController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${python.service.url}")
    private String pythonServiceURL;


    @PostMapping("/interact/{top_k}")
     public ResponseEntity<?> queryDoc(@RequestBody Question question, @PathVariable int top_k){
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String User=auth.getName();
         String Uid= userRepository.findByuserName(User).getId().toString();
         try{
             String queryUrl=pythonServiceURL + "/documents/query"
                     + "?user_id=" + Uid
                     + "&question=" + question.getQuestion()
                     + "&top_k=" + top_k;

             ResponseEntity<Map> result=restTemplate.exchange(queryUrl, HttpMethod.GET, HttpEntity.EMPTY, Map.class);


             Map<String, Object> body = result.getBody();
             Map<String, Object> ragResponse = (Map<String, Object>) body.get("response");

             Map<String, Object> cleanResponse = new LinkedHashMap<>();
             cleanResponse.put("query", question.getQuestion());
             cleanResponse.put("answer", ragResponse.get("answer"));
             cleanResponse.put("sources", ragResponse.get("chunks"));

             return new ResponseEntity<>(cleanResponse, HttpStatus.OK);
         }catch (Exception e) {
             return new ResponseEntity<>("Failed to retrieve documents", HttpStatus.INTERNAL_SERVER_ERROR);
         }
    }



     }

