package com.organize.FileOrganizer.controller;

import com.organize.FileOrganizer.entity.file;
import com.organize.FileOrganizer.service.fileService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file")
public class fileController {
    @Autowired
    private fileService fileService;
    @PostMapping("/upload/{folderName}")
    public ResponseEntity<?>  uploadFile(@RequestParam MultipartFile file, @PathVariable String folderName) throws IOException {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String uname=auth.getName();
        ResponseEntity<?> fileUpload=fileService.saveFile(file,uname,folderName);
        return  fileUpload;
    }

    //Endpoint to get all files of a folder
    @GetMapping("/folder/{folderName}")
    public List<file> getFiles(@PathVariable String folderName){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String uname=auth.getName();
       List<file> files= fileService.getFiles(uname,folderName);
        return files;
    }

    @PutMapping("/rename/{folderName}/{OGfileName}/{newName}")
    public String Renamefile(@PathVariable String folderName,@PathVariable String OGfileName,@PathVariable String newName) throws IOException{
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String uname=auth.getName();
        String response= fileService.renameFolder(uname,folderName,OGfileName,newName);
        return response;
    }
    @GetMapping("/open/{fileId}")
    public ResponseEntity<Resource> openFile(@PathVariable ObjectId fileId) throws IOException {
        ResponseEntity<Resource> httpResponse= fileService.openFile(fileId);
        return  httpResponse;
    }

    //Moving a file to another folder
    @PutMapping("/move/{folderName}/{newFolderName}/{fileName}")
   public ResponseEntity<?> moveFolder (  @PathVariable  String folderName, @PathVariable  String newFolderName, @PathVariable  String fileName) throws IOException {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String uname=auth.getName();
       return fileService.moveFile(uname, folderName, newFolderName, fileName);
    }

    @DeleteMapping("/delete/{folderName}/{fileName}")
    public ResponseEntity<?> delelteFile(@PathVariable String foldername,@PathVariable String fileName) throws IOException {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String uname=auth.getName();
        ResponseEntity<?> delfile= fileService.deleteFile(uname,foldername,fileName);
        return delfile;
    }
}
