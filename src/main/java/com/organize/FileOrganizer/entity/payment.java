package com.organize.FileOrganizer.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
public class payment {
    @Id
    private ObjectId id;

    private String razorpayOrderId;

    private String username;

    private long amount;

    private String planName;

    private String status;

    private LocalDateTime createdAt;
}
