package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.dto.ApiResponse;
import com.ecommerce.authservice.dto.LoginRequest;
import com.ecommerce.authservice.dto.RegisterRequest;
import com.ecommerce.authservice.exception.InvalidCredentialException;
import com.ecommerce.authservice.model.Role;
import com.ecommerce.authservice.model.User;
import com.ecommerce.authservice.repository.UserRepository;
import com.ecommerce.authservice.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,PasswordEncoder passwordEncoder,JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request){

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email already registered");
        }

        // 1️⃣ Read role from request
        String roleFromRequest = request.getRole();
        // 2️⃣ VALIDATE + NORMALIZE ROLE  ✅ ADD HERE
        Role role;
        try{
            role = Role.valueOf(roleFromRequest.toUpperCase());
        }catch (IllegalArgumentException | NullPointerException ex){
            throw new IllegalArgumentException("Invalid role. Allowed values: USER, SELLER,ADMIN");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(role);


        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User Registered Successfully");


    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialException("Invalid Email or Password"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new InvalidCredentialException("Invalid Email or Password");
        }

        String token = jwtUtil.generateToken(user.getId(),user.getEmail(),user.getRole());

        return ResponseEntity.ok(new ApiResponse(200,token));

    }



}
