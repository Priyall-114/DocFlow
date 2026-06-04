package com.organize.FileOrganizer.controller;

import com.organize.FileOrganizer.Repository.UserRepository;
import com.organize.FileOrganizer.entity.folder;
import com.organize.FileOrganizer.entity.user;
import com.organize.FileOrganizer.service.folderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/folder")
public class folderController {

    @Autowired
    private folderService folderService;

    @PostMapping("/createFolder/{username}")
    public ResponseEntity<?> createFolder(@RequestBody folder Folder, @PathVariable String username){
        Folder.setCreateddate(LocalDateTime.now());

       ResponseEntity<?> response= folderService.saveFolder(Folder,username);
        return response;
    }

    //Endpoint to get all folders
    @GetMapping("/allfolders")
    public List<folder> getAllFolders(){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String uname=auth.getName();
        List<folder> folders=folderService.Listfolders(uname);
        return folders;
    }
    @PutMapping("/rename/{OGfolderName}/{newName}")
    public String Renamefolder(@PathVariable String OGfolderName,@PathVariable String newName) throws IOException {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String uname=auth.getName();
       String response= folderService.renameFolder(uname,OGfolderName,newName);
        return response;
    }

    @DeleteMapping("/delete/{foname}")
    public void delelteFolder(@PathVariable String foname) throws IOException {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String uname=auth.getName();
            folderService.deleteFolder(uname,foname);
    }
}
