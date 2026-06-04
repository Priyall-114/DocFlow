package com.organize.FileOrganizer.Filter;

import com.organize.FileOrganizer.service.UserDetailServiceImpl;
import com.organize.FileOrganizer.utils.jwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class jwtFilter extends OncePerRequestFilter {
    @Autowired
    private jwtUtils jwtUtils;
    @Autowired
    private UserDetailServiceImpl userDetailServiceimpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader=request.getHeader("Authorization");
        String username=null;
        String jwt=null;
        if(authorizationHeader!=null && authorizationHeader.startsWith("Bearer")){jwt=authorizationHeader.substring(7);
        username=jwtUtils.extractUsername(jwt);
        }
        if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetails=userDetailServiceimpl.loadUserByUsername(username);
            //validates checked expiry of token
            if(jwtUtils.validateToken(jwt)){

                UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}
