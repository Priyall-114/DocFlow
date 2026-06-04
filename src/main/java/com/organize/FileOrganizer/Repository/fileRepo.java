package com.organize.FileOrganizer.Repository;

import com.organize.FileOrganizer.entity.file;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface fileRepo extends MongoRepository<file, ObjectId> {
    boolean existsByFolderidAndFileName(ObjectId folderid,String fileName);
    List<file> findByFolderidAndUserid(ObjectId folderid, ObjectId userid);
    List<file> findByUseridAndFileName( ObjectId userid,String fileName);
    file findByUseridAndFolderidAndFileName(ObjectId userid,ObjectId folderid,String fileName);
    file findByFileId(ObjectId fileId);
}
