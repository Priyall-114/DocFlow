package com.organize.FileOrganizer.controller;

import  com.organize.FileOrganizer.service.RazorpayService;
import com.organize.FileOrganizer.service.RazorpayService;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class RazorpayController {
    @Autowired
private RazorpayService razorpayService;

    @GetMapping("/view-plans")
    public ResponseEntity<?> viewPlans(){

        List<Map<String,Object>> plans = new ArrayList<>();

        Map<String,Object> basic = new HashMap<>();

        basic.put("planName","basic");

        basic.put("storage", "1 GB");
        basic.put("price", "Rs49");

        plans.add(basic);

        Map<String,Object> pro = new HashMap<>();

        pro.put("planName","pro");

        pro.put("storage", "5 GB");

        pro.put("price", "Rs199");

        plans.add(pro);

        return ResponseEntity.ok(plans);
    }



    @PostMapping("/create-order/{planName}")
    public String createOrder(@PathVariable String planName)
            throws Exception {

        return razorpayService
                .createOrder(planName);
    }




    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            razorpayService.handleWebhook(payload, signature);
            return ResponseEntity.ok("Payment verified, storage upgraded");
        } catch (Exception e) {
            log.error("Webhook verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        }
    }
}
