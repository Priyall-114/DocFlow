package com.organize.FileOrganizer.SpringSecurity;

import com.organize.FileOrganizer.Filter.jwtFilter;
import com.organize.FileOrganizer.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private  UserDetailServiceImpl userDetailServiceimpl;
    @Autowired
    private jwtFilter jwtFilter;
@Bean
    SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
    http.authorizeHttpRequests(auth->auth.requestMatchers("/public/**","/").permitAll()
            .anyRequest().authenticated()
    );
    http.authenticationProvider(authenticationProvider(userDetailServiceimpl,passwordEncoder())).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    http.csrf(csrf->csrf.disable());
    return http.build();
}
//getting authentication engine
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
    return config.getAuthenticationManager();
}

@Bean
    public PasswordEncoder passwordEncoder(){
    return  new BCryptPasswordEncoder();
}
@Bean
    public AuthenticationProvider authenticationProvider(UserDetailServiceImpl userDetailServiceimpl , PasswordEncoder passwordEncoder){
    DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailServiceimpl);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
}


}
