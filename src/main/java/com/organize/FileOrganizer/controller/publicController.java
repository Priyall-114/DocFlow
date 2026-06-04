package com.organize.FileOrganizer.controller;

import com.organize.FileOrganizer.dto.AuthRequest;
import com.organize.FileOrganizer.entity.user;
import com.organize.FileOrganizer.service.userService;
import com.organize.FileOrganizer.utils.jwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequestMapping("/public")
@RestController
@Slf4j
public class publicController {
    @Autowired
    private userService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AuthRequest authRequest;
    @Autowired
    private jwtUtils jwtUtils;
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody user User) {
        try {
            //Password encoding left!
            userService.SaveEntry(User);
            String uname=User.getUserName();
            log.info("New User created: {}",uname);

            return new ResponseEntity<>("Signup Successful ",HttpStatus.CREATED);

        } catch (Exception e) {
            log.error("Entry could'nt be created",e);
            return new ResponseEntity<>("Username already existed try with a different userName",HttpStatus.BAD_REQUEST);
        }

    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest){
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(),authRequest.getPassword())
            );

            String token=jwtUtils.generateToken(authRequest.getUsername());

            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    "Invalid username or password"
            );
        }
    }
}

