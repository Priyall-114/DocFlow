//package com.organize.FileOrganizer.Storage;
//
//import com.razorpay.Order;
//import com.razorpay.RazorpayClient;
//import com.razorpay.RazorpayException;
//import com.razorpay.Utils;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class RazorpayService {
//    @Autowired
//    private RazorpayClient razorpayClient;
//
//    public String createOrder(int amount) throws RazorpayException{
//        JSONObject orderRequest=new JSONObject();
//        orderRequest.put("amount",amount*100);//INR-->paise
//        orderRequest.put("currency", "INR");
//        //for unique id using time stamp
//        orderRequest.put("receipt","receipt_"+System.currentTimeMillis());
//        Order order=razorpayClient.orders.create(orderRequest);
//        //we want now order Id
//        return order.get("id").toString();
//    }
//
//    @Value("${razorpay.webhook.secret}")
//    private String webhookSecret;
//    public boolean verifyWebhook(String payload,String signature){
//        try{
//            return Utils.verifyWebhookSignature(payload,signature,webhookSecret);
//        } catch (RazorpayException e) {
//            return false;
//        }
//    }
//}
