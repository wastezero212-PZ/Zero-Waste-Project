package com.example.zerowaste.service;

import com.example.zerowaste.dto.user.UserRegistrationRequestDto;
import com.example.zerowaste.dto.user.UserResponseDto;
import java.util.List;

public interface UserService {

    UserResponseDto save(UserRegistrationRequestDto requestDto);

    List<UserResponseDto> findAll();

    UserResponseDto getById(Long id);

    UserResponseDto updateById(Long id, UserRegistrationRequestDto requestDto);

    void deleteById(Long id);
}
