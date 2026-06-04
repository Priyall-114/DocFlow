package com.organize.FileOrganizer.Repository;

import com.organize.FileOrganizer.entity.payment;
import com.razorpay.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepo extends MongoRepository<payment,String> {
    payment findByRazorpayOrderId(String razorpayOrderId);

}
