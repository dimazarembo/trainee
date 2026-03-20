package by.dzarembo.trainee.controller;

import by.dzarembo.trainee.dto.*;
import by.dzarembo.trainee.mapper.PaymentCardMapper;
import by.dzarembo.trainee.mapper.UserMapper;
import by.dzarembo.trainee.security.AccessChecker;
import by.dzarembo.trainee.service.PaymentCardService;
import by.dzarembo.trainee.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PaymentCardService paymentCardService;
    private final PaymentCardMapper paymentCardMapper;
    private final AccessChecker accessChecker;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        accessChecker.checkUserAccess(id);
        return ResponseEntity.ok(userMapper.toResponse(userService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable
    ) {
        Page<UserResponse> response = userService.getAll(name, surname, pageable).map(userMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/with-cards")
    public ResponseEntity<UserWithCardsResponse> getUserWithCards(@PathVariable Long userId) {
        accessChecker.checkUserAccess(userId);
        return ResponseEntity.ok(userService.getUserWithCards(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(userService.create(userMapper.toEntity(userCreateRequest))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        accessChecker.checkUserAccess(id);
        return ResponseEntity.ok(userMapper.toResponse(userService.update(id, userMapper.toEntity(userUpdateRequest))));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        accessChecker.checkUserAccess(id);
        return ResponseEntity.ok(userMapper.toResponse(userService.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        accessChecker.checkUserAccess(id);
        return ResponseEntity.ok(userMapper.toResponse(userService.deactivate(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        accessChecker.checkUserAccess(id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{userId}/cards")
    public ResponseEntity<List<PaymentCardResponse>> getCardsByUser(@PathVariable Long userId) {
        accessChecker.checkUserAccess(userId);

        List<PaymentCardResponse> response = paymentCardService.getAllByUserId(userId).stream().map(paymentCardMapper::toResponse).toList();

        return ResponseEntity.ok(response);
    }

}
