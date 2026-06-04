package com.organize.FileOrganizer.entity;

import com.organize.FileOrganizer.enums.FileStatus;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Data
@Document(collection = "File")
public class file {
    @Id
    private ObjectId fileId;
    private ObjectId folderid;
    //@Indexed(unique = true)
    private String fileName;

    private String fileType;
    private String filePath;
    private LocalDateTime uploadDate;
    private LocalDateTime lastModifiedDate;
    private FileStatus status;
    private Long FileSize;
    //Since all files always won't belong to a folder
    private ObjectId userid;
}
