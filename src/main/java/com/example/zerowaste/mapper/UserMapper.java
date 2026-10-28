package com.example.zerowaste.mapper;

import com.example.zerowaste.config.MapperConfig;
import com.example.zerowaste.dto.user.UserRegistrationRequestDto;
import com.example.zerowaste.dto.user.UserResponseDto;
import com.example.zerowaste.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toEntity(UserRegistrationRequestDto requestDto);

    UserResponseDto toDto(User user);

    void updateUserFromDto(UserRegistrationRequestDto dto, @MappingTarget User user);
}
