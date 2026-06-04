package com.organize.FileOrganizer.service;

import com.organize.FileOrganizer.Repository.UserRepository;
import com.organize.FileOrganizer.Repository.fileRepo;
import com.organize.FileOrganizer.Repository.folderRepo;
import com.organize.FileOrganizer.entity.file;
import com.organize.FileOrganizer.entity.folder;
import com.organize.FileOrganizer.entity.user;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class folderService {
    @Autowired
    private folderRepo folderRepo;
    @Autowired
    private fileRepo fileRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private fileService fileService;
    @Autowired
    private s3service s3service;

    public ResponseEntity<?> saveFolder(folder Folder, String uname) {
        user User = userRepository.findByuserName(uname);
        if (User == null) {
            return new ResponseEntity<>("User not found :(", HttpStatus.NOT_FOUND);
        }

        boolean exists = folderRepo.existsByUserIdAndFolderName(User.getId(), Folder.getFolderName());
        if (!exists) {
            Folder.setUserId(User.getId());
            folderRepo.save(Folder);
            log.info("folder created");
            return new ResponseEntity<>("Folder Created", HttpStatus.CREATED);
        } else {
            log.error("Folder with same name exists");
            return new ResponseEntity<>("The folder name already exists", HttpStatus.FORBIDDEN);
        }


    }

    public List<folder> Listfolders(String uname) {
        user User = userRepository.findByuserName(uname);
        List<folder> folders = folderRepo.findByUserId(User.getId());
        return folders;
    }

    public String renameFolder(String uname, String folderName, String newName) throws IOException {
        user User = userRepository.findByuserName(uname);
        folder Folder = folderRepo.findByUserIdAndFolderName(User.getId(), folderName);
        boolean exists = folderRepo.existsByUserIdAndFolderName(User.getId(), newName);
        if (!exists) {
            List<file> Files = fileRepo.findByFolderidAndUserid(Folder.getFolderid(), User.getId());
            for(file f : Files){
                String oldS3key= uname + "/" + folderName + "/" + f.getFileName();
                String newS3Key = uname + "/" + newName + "/" + f.getFileName();

                s3service.copyFile(oldS3key,newS3Key);
                s3service.deleteFile(oldS3key);
                String newS3url="https://docfloww.s3.ap-south-1.amazonaws.com/"+newS3Key;
                f.setFilePath(newS3url);
                fileRepo.save(f);
            }
            Folder.setFolderName(newName);
            folderRepo.save(Folder);

            return "Folder renamed to" + newName;
        } else {
            return "Can't rename folder with this name already exists" + newName;
        }
    }

    //We will have to cascade delete
    public String deleteFolder(String uname, String folderName) throws IOException {
        if (folderName.equals("Random")) {
            return "Can't delete Random it is systems default folder";
        } else {
            user User = userRepository.findByuserName(uname);

        folder Folder = folderRepo.findByUserIdAndFolderName(User.getId(), folderName);
        List<file> Files = fileRepo.findByFolderidAndUserid(Folder.getFolderid(), User.getId());
        for (int i = 0; i < Files.size(); i++) {
            User.setCurrentAvailableSpace(User.getCurrentAvailableSpace()+ Files.get(i).getFileSize());
            userRepository.save(User);
            ResponseEntity<?> deletedFiles = fileService.cascadeDelete(Files.get(i));
            log.info("Deleted file {}", deletedFiles);
        }

        folderRepo.deleteById(Folder.getFolderid());
        return "Deleted folder";
    }


}
    }

