package com.organize.FileOrganizer.Repository;

import com.organize.FileOrganizer.entity.folder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface folderRepo extends MongoRepository<folder, ObjectId> {
    //Searches for userid and folder if already present can't create
    boolean existsByUserIdAndFolderName(ObjectId userId,String folderName);
    folder findByUserIdAndFolderName(ObjectId userId,String folderName);
    List<folder> findByUserId(ObjectId userId);
    folder findByFolderName(String folderName);
    folder findByFolderid(ObjectId folderid);
}
