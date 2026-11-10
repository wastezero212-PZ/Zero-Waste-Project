package com.example.zerowaste.service.impl;

import com.example.zerowaste.dto.user.UserRegistrationRequestDto;
import com.example.zerowaste.dto.user.UserResponseDto;
import com.example.zerowaste.exception.EntityNotFoundException;
import com.example.zerowaste.exception.RegistrationException;
import com.example.zerowaste.mapper.UserMapper;
import com.example.zerowaste.model.Role;
import com.example.zerowaste.model.ShoppingCart;
import com.example.zerowaste.model.User;
import com.example.zerowaste.repository.RoleRepository;
import com.example.zerowaste.repository.ShoppingCartRepository;
import com.example.zerowaste.repository.UserRepository;
import com.example.zerowaste.service.UserService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ShoppingCartRepository cartRepository;

    @Override
    public UserResponseDto save(UserRegistrationRequestDto requestDto) {
        requestDto.setEmail(requestDto.getEmail().toLowerCase());
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException(
                    "User with email: "
                            + requestDto.getEmail()
                            + " already exists.");
        }
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        User user = userMapper.toEntity(requestDto);

        if (roleRepository.findByName(Role.RoleName.USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.USER);
            roleRepository.save(userRole);
        }

        Role roleUser = roleRepository.findByName(Role.RoleName.USER).orElseThrow(
                () -> new EntityNotFoundException("Can't find role with type USER")
        );

        user.setRoles(Set.of(roleUser));

        User savedUser = userRepository.save(user);

        ShoppingCart cart = new ShoppingCart();
        cart.setOwner(savedUser);
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return userMapper.toDto(savedUser);
    }

    @Override
    public Page<UserResponseDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
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

    @Override
    @Transactional
    public UserResponseDto createAdmin() {

        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("Admin@admin.com");
        requestDto.setPassword("adminPassword");
        requestDto.setRepeatPassword("adminPassword");
        requestDto.setFirstName("admin");
        requestDto.setLastName("admin");

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

        if (roleRepository.findByName(Role.RoleName.USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.USER);
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName(Role.RoleName.ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ADMIN);
            roleRepository.save(adminRole);
        }

        Role roleAdmin = roleRepository.findByName(Role.RoleName.ADMIN).orElseThrow(
                () -> new EntityNotFoundException("Can't find role with type ADMIN")
        );

        Role roleUser = roleRepository.findByName(Role.RoleName.USER).orElseThrow(
                () -> new EntityNotFoundException("Can't find role with type USER")
        );

        user.setRoles(Set.of(roleUser, roleAdmin));
        User saved = userRepository.save(user);

        ShoppingCart cart = new ShoppingCart();
        cart.setOwner(saved);
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return userMapper.toDto(saved);
    }
}
