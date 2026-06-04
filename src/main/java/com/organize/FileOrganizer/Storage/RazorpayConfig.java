package com.organize.FileOrganizer.Storage;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {
    @Value("${razorpay.key.id}")
    private String KeyID;
    @Value("${razorpay.key.secret}")
    private String keySecret;


//    RazorpayClient-object communicates with Razorpay APIs.
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException{
        return new RazorpayClient(KeyID,keySecret);
    }
}

//my Backend → (credentials + order details) → Razorpay Servers
//Razorpay Servers → (Order ID) → my Backend
//my Backend → (Order ID) → User
//User → Razorpay pe payment → Webhook → myBackend
