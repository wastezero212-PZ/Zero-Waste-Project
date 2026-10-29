package com.example.zerowaste.controller;

import com.example.zerowaste.dto.user.UserRegistrationRequestDto;
import com.example.zerowaste.dto.user.UserResponseDto;
import com.example.zerowaste.model.User;
import com.example.zerowaste.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Controller", description = "Endpoints for managing users")
@RestController
@AllArgsConstructor
@RequestMapping(value = "api/users")
public class UserController {

    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get currently logged in user.")
    public UserResponseDto getLoggedInUser(@AuthenticationPrincipal User user) {
        return userService.getById(user.getId());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get user by user id.")
    public UserResponseDto getLoggedInUser(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update logged in user.")
    public UserResponseDto updateUser(
            @AuthenticationPrincipal User user,
            @RequestBody UserRegistrationRequestDto userRequestDto) {
        return userService.updateById(user.getId(), userRequestDto);
    }

    // Admin endpoints

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all users paginated.")
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update user by user id.")
    public UserResponseDto updateUserById(
            @PathVariable Long id,
            @RequestBody UserRegistrationRequestDto userRequestDto) {
        return userService.updateById(id, userRequestDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user by user id.")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
    }
}
