package com.example.zerowaste.controller.testcontroller;

import com.example.zerowaste.dto.category.ProductCategoryRequestDto;
import com.example.zerowaste.dto.product.ProductRequestDto;
import com.example.zerowaste.dto.user.UserRegistrationRequestDto;
import com.example.zerowaste.model.User;
import com.example.zerowaste.repository.ProductCategoryRepository;
import com.example.zerowaste.repository.UserRepository;
import com.example.zerowaste.service.ProductCategoryService;
import com.example.zerowaste.service.ProductService;
import com.example.zerowaste.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test Controller", description = "Endpoint for setting up test data")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserService userService;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final ProductCategoryService categoryService;
    private final ProductCategoryRepository categoryRepository;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create test data")
    @Transactional
    public void setupData() {

        userService.createAdmin();

        UserRegistrationRequestDto bobRequest = createUser("bob@gmail.com", "bob123", "Bob", "Bobson");
        userService.save(bobRequest);
        UserRegistrationRequestDto aliceRequest = createUser("alice@gmail.com", "alice123", "Alice", "Alison");
        userService.save(aliceRequest);
        UserRegistrationRequestDto johnRequest = createUser("john@gmail.com", "john123", "John", "Johnson");
        userService.save(johnRequest);

        categoryService.save(createCategory("Electronics", "Electronical devices."));
        categoryService.save(createCategory("Clothing", "Everyday fashion and other clothing items."));
        categoryService.save(createCategory("Motorisation", "Used cars, spare parts and so on."));

        Long electronicsId = categoryRepository.findByName("Electronics").orElseThrow().getId();
        Long clothingId = categoryRepository.findByName("Clothing").orElseThrow().getId();
        Long motorisationId = categoryRepository.findByName("Motorisation").orElseThrow().getId();

        User bob = userRepository.findByEmail("bob@gmail.com").orElseThrow();
        User alice = userRepository.findByEmail("alice@gmail.com").orElseThrow();
        User john = userRepository.findByEmail("john@gmail.com").orElseThrow();

        ProductRequestDto tshirt = createProduct(
                "Tshirt",
                "blue t-shirt size M",
                BigDecimal.valueOf(20),
                new HashSet<>(Set.of(clothingId)));
        productService.save(tshirt, bob.getEmail());

        ProductRequestDto iphone = createProduct(
                "IPhone Duo",
                "Foldable phone from apple",
                BigDecimal.valueOf(5000),
                new HashSet<>(Set.of(electronicsId)));
        productService.save(iphone, bob.getEmail());

        ProductRequestDto shoes = createProduct(
                "Nike Air Force 36",
                "White Nike Sneakers size eu36, barely worn.",
                BigDecimal.valueOf(400),
                new HashSet<>(Set.of(clothingId)));
        productService.save(shoes, alice.getEmail());

        ProductRequestDto helmet = createProduct(
                "Motorbike Helmet",
                "Red helmet, size M",
                BigDecimal.valueOf(100),
                new HashSet<>(Set.of(clothingId, motorisationId)));
        productService.save(helmet, john.getEmail());

        ProductRequestDto carRadio = createProduct(
                "Car Radio",
                "Working car radio from sony",
                BigDecimal.valueOf(80),
                new HashSet<>(Set.of(electronicsId, motorisationId)));
        productService.save(carRadio, john.getEmail());
    }

    private UserRegistrationRequestDto createUser(String email, String password, String firstName, String lastName) {

        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail(email);
        requestDto.setPassword(password);
        requestDto.setRepeatPassword(password);
        requestDto.setLastName(lastName);
        requestDto.setFirstName(firstName);
        return requestDto;
    }

    private ProductRequestDto createProduct(
            String title, String description, BigDecimal price, Set<Long> categoryIds) {

        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setTitle(title);
        requestDto.setDescription(description);
        requestDto.setPrice(price);
        requestDto.setCategoryIds(categoryIds);

        return requestDto;
    }

    private ProductCategoryRequestDto createCategory(String name, String description) {
        ProductCategoryRequestDto requestDto = new ProductCategoryRequestDto();
        requestDto.setName(name);
        requestDto.setDescription(description);
        return requestDto;
    }
}
