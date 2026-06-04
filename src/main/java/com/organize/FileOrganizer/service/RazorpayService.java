package com.organize.FileOrganizer.service;

import com.organize.FileOrganizer.Repository.PaymentRepo;
import com.organize.FileOrganizer.Repository.UserRepository;
import com.organize.FileOrganizer.Storage.RazorpayConfig;
import com.organize.FileOrganizer.entity.payment;
import com.organize.FileOrganizer.entity.user;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class RazorpayService {

    private String keySecret;
    @Autowired
    private PaymentRepo paymentRepo;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RazorpayConfig razorpayConfig;


    //OrderCreation
    public String createOrder(String planName) throws Exception {
        Authentication auht = SecurityContextHolder.getContext().getAuthentication();
        String username = auht.getName();
        long amount;
        if (planName.equals("basic")) {
            amount = 49;
        } else if (planName.equals("pro")) {
            amount = 199;
        } else {
            return "Invalid Plan";
        }

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);//INR-->paise
        orderRequest.put("currency", "INR");
        //for unique id using time stamp
        orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());

        Order order = razorpayConfig.razorpayClient().orders.create(orderRequest);

        payment paymentobj = new payment();
        paymentobj.setRazorpayOrderId(order.get("id").toString());

        paymentobj.setUsername(username);

        paymentobj.setAmount(amount);

        paymentobj.setPlanName(planName);

        paymentobj.setStatus("PENDING");

        paymentobj.setCreatedAt(
                LocalDateTime.now()
        );

        paymentRepo.save(paymentobj);
        return order.toString();
    }




    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public void handleWebhook(String payload, String signature) throws RazorpayException {
        // Verify signature
        boolean isValid = com.razorpay.Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        if (!isValid) throw new RazorpayException("Invalid signature");
        JSONObject payloadJson = new JSONObject(payload);
        String orderId = payloadJson
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getString("order_id");

        //finding the payment
        payment paymentObj = paymentRepo.findByRazorpayOrderId(orderId);
        paymentObj.setStatus("COMPLETED");
        paymentRepo.save(paymentObj);

        //updating user stoage
     user User=userRepository.findByuserName(paymentObj.getUsername());
     if(User!=null){
         if (paymentObj.getPlanName().equals("basic")){
             User.setCurrentAvailableSpace(User.getCurrentAvailableSpace()+1073741824L);
         }
         else if(paymentObj.getPlanName().equals("pro")){
             User.setCurrentAvailableSpace(User.getCurrentAvailableSpace()+5368709120L);
         }
     }
        userRepository.save(User);
    }
}