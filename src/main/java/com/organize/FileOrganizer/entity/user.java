package com.organize.FileOrganizer.entity;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
@Data
@Document(collection = "User")
public class user {
    @Indexed(unique = true)
    String userName;
    String password;
    Long CurrentAvailableSpace= 104857600L;

    @Indexed(unique = true)
    String email;
    @Id
    private ObjectId id;

}
