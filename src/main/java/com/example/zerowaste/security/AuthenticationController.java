package com.example.zerowaste.security;

import com.example.zerowaste.dto.user.UserLoginRequestDto;
import com.example.zerowaste.dto.user.UserLoginResponseDto;
import com.example.zerowaste.dto.user.UserRegistrationRequestDto;
import com.example.zerowaste.dto.user.UserResponseDto;
import com.example.zerowaste.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication Manager", description = "Endpoints for managing user registration")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Log into an existing user account.")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user.")
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto request) {
        return userService.save(request);
    }
}
