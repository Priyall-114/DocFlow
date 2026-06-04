package com.organize.FileOrganizer.service;

import com.organize.FileOrganizer.Repository.UserRepository;
import com.organize.FileOrganizer.controller.folderController;
import com.organize.FileOrganizer.entity.folder;
import com.organize.FileOrganizer.entity.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class userService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private folderService folderService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public void SaveEntry(user User){
        User.setPassword(passwordEncoder.encode(User.getPassword()));
        userRepository.save(User);
        folder randomFolder=new folder();
        randomFolder.setFolderName("Random");
        ResponseEntity<?> random_folder= folderService.saveFolder(
                randomFolder
            ,User.getUserName());
    }

}
