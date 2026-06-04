package com.organize.FileOrganizer.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Data
@Document(collection = "Folder")
public class folder {
    @Id
    private ObjectId folderid;
    //@Indexed(unique = true)
    private String folderName;
    private LocalDateTime Createddate;
    private ObjectId userId;

}
