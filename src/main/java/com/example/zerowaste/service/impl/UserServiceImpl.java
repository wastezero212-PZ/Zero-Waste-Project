package com.example.zerowaste.service.impl;

import com.example.zerowaste.dto.user.UserRegistrationRequestDto;
import com.example.zerowaste.dto.user.UserResponseDto;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.exception.RegistrationException;
import com.example.zerowaste.mapper.UserMapper;
import com.example.zerowaste.model.Role;
import com.example.zerowaste.model.User;
import com.example.zerowaste.repository.RoleRepository;
import com.example.zerowaste.repository.UserRepository;
import com.example.zerowaste.service.UserService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto save(UserRegistrationRequestDto requestDto) {
        requestDto.setEmail(requestDto.getEmail().toLowerCase());
        if (userRepository.findByEmail(
                requestDto.getEmail()).isPresent()) {
            throw new RegistrationException(
                    "User with email: "
                    + requestDto.getEmail()
                    + " already exists.");
        }
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        User user = userMapper.toEntity(requestDto);
        Role roleUser = roleRepository.findByName(Role.RoleName.USER).orElseThrow(
                () -> new EntityNotFoundException("Can't find role with type USER")
        );
        user.setRoles(Set.of(roleUser));
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    public List<UserResponseDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserResponseDto getById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot find user with id: " + id)
        );
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateById(Long id, UserRegistrationRequestDto requestDto) {
        User existingUser = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot update. User not found with id: " + id)
        );

        if (!existingUser.getEmail().equalsIgnoreCase(requestDto.getEmail())
                && userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException(
                    "Email " + requestDto.getEmail() + " is already in use.");
        }
        userMapper.updateUserFromDto(requestDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Cannot find user with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
