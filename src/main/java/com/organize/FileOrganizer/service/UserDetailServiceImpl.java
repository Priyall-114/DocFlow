package com.organize.FileOrganizer.service;

import com.organize.FileOrganizer.Repository.UserRepository;
import com.organize.FileOrganizer.entity.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username){
        user User=userRepository.findByuserName(username);
        if(User!=null){
            return org.springframework.security.core.userdetails.User.builder()
                    .username(User.getUserName())
                    .password(User.getPassword())
                    .build();
        }
        throw new UsernameNotFoundException("User not found with username"+username);
    }
}
