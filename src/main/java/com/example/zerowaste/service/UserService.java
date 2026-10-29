package com.example.zerowaste.service;

import com.example.zerowaste.dto.user.UserRegistrationRequestDto;
import com.example.zerowaste.dto.user.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDto save(UserRegistrationRequestDto requestDto);

    Page<UserResponseDto> findAll(Pageable pageable);

    UserResponseDto getById(Long id);

    UserResponseDto updateById(Long id, UserRegistrationRequestDto requestDto);

    void deleteById(Long id);
}
