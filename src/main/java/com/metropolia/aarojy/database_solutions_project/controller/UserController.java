package com.metropolia.aarojy.database_solutions_project.controller;

import com.metropolia.aarojy.database_solutions_project.Security.JwtService;
import com.metropolia.aarojy.database_solutions_project.dto.LoginDTO;
import com.metropolia.aarojy.database_solutions_project.dto.RegisterDTO;
import com.metropolia.aarojy.database_solutions_project.entity.AppUser;
import com.metropolia.aarojy.database_solutions_project.entity.Customer;
import com.metropolia.aarojy.database_solutions_project.repository.CustomerRepository;
import com.metropolia.aarojy.database_solutions_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDTO regDto) {

        if (userRepository.findByUsername(regDto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        AppUser newUser = new AppUser();
        newUser.setUsername(regDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(regDto.getPassword()));
        newUser.setRole("USER");

        AppUser savedUser = userRepository.save(newUser);

        Customer customer = new Customer();
        customer.setFirst_name(regDto.getFirstName());
        customer.setLast_name(regDto.getLastName());
        customer.setEmail(regDto.getEmail());
        customer.setPhone(regDto.getPhone());
        customer.setAppUser(savedUser);

        customerRepository.save(customer);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {

        AppUser user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(Map.of("token", token));
    }
}
