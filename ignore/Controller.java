//package com.organize.FileOrganizer.Storage;
//
//import com.razorpay.RazorpayException;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Signature;
//
//@RestController
//@RequestMapping("/storage")
//public class Controller {
//    @Autowired
//    private RazorpayService razorpayService;
//    @GetMapping("/showPlans")
//    public String seePlans(){
//        return "1GB         Rs 49 /n 2GB            Rs 99 /n 5GB            Rs200";
//    }
//
//    @PostMapping("/createOrder")
//    public ResponseEntity<String> createOrder(@RequestParam int amount){
//        try{
//            String orderId= razorpayService.createOrder(amount);
//            return ResponseEntity.ok(orderId);
//        } catch (RazorpayException e) {
//            return ResponseEntity.status(500).body("Order creation failed");
//        }
//    }
//    //controller for razorpay repsonse Jab bhi koi payment ho, mere is URL pe aa ke bata dena"
//    @PostMapping("/webhook")
//    public ResponseEntity<?> handleWebhook(@RequestBody String payload,@RequestHeader("X-Razorpay-Signature") String signature) {
//        // Razorpay payment hone ke baad yahan request bhejta hai
//        // payload — payment ki details
//        // signature — verify karne ke liye ki request genuine hai
//        boolean isValid = razorpayService.verifyWebhook(payload, signature);
//        if (isValid) {
//            {
//                //to get info parsing payload object
//                JSONObject payloadJson=new JSONObject(payload);
//
//                return ResponseEntity.ok("Payment verified");
//            }
//
//        }
//        return ResponseEntity.status(400).body("Invalid signature");
//
//    }
//
//
//}
