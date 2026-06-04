package com.organize.FileOrganizer.service;

import com.organize.FileOrganizer.Repository.UserRepository;
import com.organize.FileOrganizer.Repository.fileRepo;
import com.organize.FileOrganizer.Repository.folderRepo;
import com.organize.FileOrganizer.dto.SearchResultDTO;
import com.organize.FileOrganizer.entity.file;
import com.organize.FileOrganizer.entity.folder;
import com.organize.FileOrganizer.entity.user;
import com.organize.FileOrganizer.enums.FileStatus;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class fileService {
    @Autowired
    private folderRepo folderRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private fileRepo fileRepo;
    @Autowired
    private s3service s3service;

    public ResponseEntity<?> saveFile(MultipartFile File , String uname , String folderName) throws IOException {
        try {
            if (folderName == null) folderName = "Random";

            //S3 mein file ka path format uname/foldername/fileName
            String s3Key = uname + "/" + folderName + "/" + File.getOriginalFilename();//S3 mein file ka address
            user User = userRepository.findByuserName(uname);
            folder Folder = folderRepo.findByUserIdAndFolderName(User.getId(), folderName);
            if(Folder == null) {
                return new ResponseEntity<>("Folder does not exist", HttpStatus.NOT_FOUND);
            }
            boolean exists = fileRepo.existsByFolderidAndFileName(Folder.getFolderid(), File.getOriginalFilename());
            if (!exists && User.getCurrentAvailableSpace() >= File.getSize()) {
                try {
                    String s3URL = s3service.uploadFile(
                            s3Key,
                            File.getInputStream(),
                            File.getSize(),
                            File.getContentType()
                    );
                    log.info("Successfully Uploaded file");
                    //s3Url is the file location metadata thta will go tot monogDB
                    file Fileobj = new file();
                    Fileobj.setFilePath(s3URL);
                    Fileobj.setFileName(File.getOriginalFilename());
                    Fileobj.setFileSize(File.getSize());
                    Fileobj.setFolderid(Folder.getFolderid());
                    if (Folder.getFolderName().equals("Random")) Fileobj.setStatus(FileStatus.PENDING);
                    else {
                        Fileobj.setStatus(FileStatus.ORGANIZED);
                    }
                    Fileobj.setUserid(User.getId());
                    Fileobj.setUploadDate(LocalDateTime.now());
                    Fileobj.setLastModifiedDate(LocalDateTime.now());
                    fileRepo.save(Fileobj);
                    User.setCurrentAvailableSpace(User.getCurrentAvailableSpace() - Fileobj.getFileSize());
                    userRepository.save(User);
                } catch (Exception e) {
                    if (User.getCurrentAvailableSpace() >= File.getSize()) {
                        return new ResponseEntity<>("Your free space usage is exhausted please check storage for more info /n File couldn't be saved", HttpStatus.BAD_REQUEST);
                    }
                    return new ResponseEntity<>("File Name already exists", HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>("File uploaded:)", HttpStatus.OK);

        } catch (Exception e) {
            log.error("File couldn't be uploaded", e);
            return new ResponseEntity<>("File couldn't be uploaded", HttpStatus.BAD_REQUEST);
        }

    }

    public List<file> getFiles(String uname, String folderName) {
        user User=userRepository.findByuserName(uname);
        folder Folder=folderRepo.findByUserIdAndFolderName(User.getId(),folderName);
        List<file> files=fileRepo.findByFolderidAndUserid(Folder.getFolderid(),User.getId());
        return files;
    }

    //get file by searching across all folders
    public List<SearchResultDTO> searchMatch(String uname, String fileName){
        user User = userRepository.findByuserName(uname);
        List<file> files = fileRepo.findByUseridAndFileName(User.getId(), fileName);

        // Final response list
        List<SearchResultDTO> result = new ArrayList<>();

        // If no files found
        if(files.isEmpty()){return result;}

        // Loop through all matching files
        for(int i = 0; i < files.size(); i++){

            // Finding folder of current file
            folder Folder =
                    folderRepo.findByFolderid(files.get(i).getFolderid());

            // Creating DTO object
            SearchResultDTO dto = new SearchResultDTO();

            // Setting DTO fields
            dto.setFileName(files.get(i).getFileName());

            dto.setFolderName(Folder.getFolderName());

            // Adding DTO into final list
            result.add(dto);
        }

        return result;
    }

    public String renameFolder(String uname, String folderName,String oGfileName, String newName) throws IOException{
        user User=userRepository.findByuserName(uname);
        folder Folder= folderRepo.findByUserIdAndFolderName(User.getId(),folderName);
        boolean exists=fileRepo.existsByFolderidAndFileName(Folder.getFolderid(),newName);
        file File=fileRepo.findByUseridAndFolderidAndFileName(User.getId(),Folder.getFolderid(),oGfileName);
        if(!exists && File!=null){
            String oldS3Key = uname + "/" + folderName + "/" + oGfileName;
            String newS3Key = uname + "/" + folderName + "/" + newName;
            s3service.copyFile(oldS3Key, newS3Key);
            s3service.deleteFile(oldS3Key);
            String newS3url="https://docfloww-files.s3.ap-south-1.amazonaws.com/"+newS3Key;
            File.setFileName(newName);

            File.setFilePath( newS3url);
            File.setLastModifiedDate(LocalDateTime.now());
             fileRepo.save(File);
        return "File renamed to"+newName;
        }
        else{
            return "Can't rename File  with this name already exists "+newName;}
    }

    public  ResponseEntity<Resource> openFile(ObjectId fileId) throws IOException {
        //getting metadata
        file File=fileRepo.findByFileId(fileId);
       String s3URL=File.getFilePath();
        File.setLastModifiedDate(LocalDateTime.now());
        fileRepo.save(File);
        Resource resource=new UrlResource(s3URL);//not just  tetx url but stores connection/access to actual file data source.readable file-access object internally url resource can open stream read bytes access file contents but path is jsut file address so not pass that in body below

        //Now in response sending complete HTTP request so constructiong it 3 parts
        //1.status 2.instrcutions type of and loading way 3.actual content
        String fileName=File.getFileName();
        String contentType=Files.probeContentType(Path.of(fileName));
        return  ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,// how file should be handled
                        "inline; filename=\""
                        +File.getFileName()
                        +"\""
                        //equivalent to http header Content-Disposition: inline; filename="notes.pdf"
                )
                .contentType(
                        MediaType.parseMediaType(contentType)
                )
                .body(resource);


    }
    //inline implies open in browser and attachement implies downolad force one

    //Moving files from one folder to another
    public ResponseEntity<String> moveFile(String uname, String folderName, String newFolderName, String fileName) throws IOException {
        user User=userRepository.findByuserName(uname);
        folder OldFolder= folderRepo.findByUserIdAndFolderName(User.getId(),folderName);
        file File=fileRepo.findByUseridAndFolderidAndFileName(User.getId(),OldFolder.getFolderid(), fileName);
        try{
            folder NewFolder = folderRepo.findByUserIdAndFolderName(User.getId(), newFolderName);
            boolean exists = fileRepo.existsByFolderidAndFileName(NewFolder.getFolderid(), fileName);
            if(exists){return new ResponseEntity<>("File already exists in destination folder",
                    HttpStatus.BAD_REQUEST);
            }
            //Old path
            String oldS3Key=uname + "/" + folderName + "/" + fileName;
            //New Path
            String newS3Key=uname + "/" + newFolderName + "/" + fileName;

            //for moving in s3 u need to copy to new location and delete from old
            s3service.copyFile(oldS3Key,newS3Key);
            s3service.deleteFile(oldS3Key);

            String newS3url="https://docfloww-files.s3.ap-south-1.amazonaws.com/"+newS3Key;

            //Updating MongoDB
            File.setFilePath(newS3url);
            File.setLastModifiedDate(LocalDateTime.now());
            File.setFolderid(NewFolder.getFolderid());
            if(newFolderName.equals("Random"))File.setStatus(FileStatus.PENDING);
            else{File.setStatus(FileStatus.ORGANIZED);}

            fileRepo.save(File);
            return new ResponseEntity<>("file moved to "+newFolderName,HttpStatus.OK);

        }catch (Exception e) {
            return new ResponseEntity<>("cannot move file to"+newFolderName+e,HttpStatus.BAD_REQUEST);
        }
    }


    //deleting specific  file
    public ResponseEntity<?> deleteFile(String uname, String folderName,String fileName) throws IOException {
        user User=userRepository.findByuserName(uname);
        folder OldFolder= folderRepo.findByUserIdAndFolderName(User.getId(),folderName);
        file File=fileRepo.findByUseridAndFolderidAndFileName(User.getId(),OldFolder.getFolderid(), fileName);
        User.setCurrentAvailableSpace(User.getCurrentAvailableSpace()+ File.getFileSize());
        userRepository.save(User);
        Path path= Path.of(File.getFilePath());
        String s3key=uname + "/" + folderName + "/" + fileName;
        s3service.deleteFile(s3key);
        fileRepo.delete(File);
        return new ResponseEntity<>("Deleted File"+fileName,HttpStatus.OK);

    }
    public ResponseEntity<?> cascadeDelete(file File) throws IOException {

        Path path= Path.of(File.getFilePath());
        String s3Url = File.getFilePath();
        // URL se key extract karo
        String s3Key = s3Url.replace("https://docfloww-files.s3.ap-south-1.amazonaws.com/", "");

        s3service.deleteFile(s3Key);
        fileRepo.delete(File);
        fileRepo.delete(File);
        return new ResponseEntity<>("Deleted File"+File.getFileName(),HttpStatus.OK);
    }
}
