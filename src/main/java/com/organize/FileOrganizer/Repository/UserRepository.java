package com.organize.FileOrganizer.Repository;

import com.organize.FileOrganizer.entity.folder;
import com.organize.FileOrganizer.entity.user;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<user, ObjectId> {
    user findByuserName(String userName);

}
