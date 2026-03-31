package by.dzarembo.trainee.controller;

import by.dzarembo.trainee.dto.PaymentCardCreateRequest;
import by.dzarembo.trainee.dto.PaymentCardResponse;
import by.dzarembo.trainee.dto.PaymentCardUpdateRequest;
import by.dzarembo.trainee.mapper.PaymentCardMapper;
import by.dzarembo.trainee.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
@AllArgsConstructor
public class PaymentCardController {
    private final PaymentCardService paymentCardService;
    private final PaymentCardMapper paymentCardMapper;

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationChecker.canAccessCard(authentication, #id)")
    public ResponseEntity<PaymentCardResponse> getPaymentCard(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardMapper.toResponse(paymentCardService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentCardResponse>> getCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable
    ) {
        Page<PaymentCardResponse> response = paymentCardService.getAll(name, surname, pageable)
                .map(paymentCardMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @authorizationChecker.isCurrentUser(authentication, #cardCreateRequest.userId)")
    public ResponseEntity<PaymentCardResponse> createPaymentCard (@Valid @RequestBody PaymentCardCreateRequest cardCreateRequest){
        return ResponseEntity.status(HttpStatus.CREATED).
                body(paymentCardMapper.toResponse(paymentCardService.create(cardCreateRequest.getUserId(), paymentCardMapper.toEntity(cardCreateRequest))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationChecker.canAccessCard(authentication, #id)")
    public ResponseEntity<PaymentCardResponse>  updatePaymentCard (@PathVariable Long id, @Valid @RequestBody PaymentCardUpdateRequest cardUpdateRequest){
        return ResponseEntity.ok(paymentCardMapper.toResponse(paymentCardService.update(id, paymentCardMapper.toEntity(cardUpdateRequest))));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("@authorizationChecker.canAccessCard(authentication, #id)")
    public ResponseEntity<PaymentCardResponse> activatePaymentCard(@PathVariable Long id){
        return ResponseEntity.ok(paymentCardMapper.toResponse(paymentCardService.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("@authorizationChecker.canAccessCard(authentication, #id)")
    public ResponseEntity<PaymentCardResponse> deactivatePaymentCard(@PathVariable Long id){
        return ResponseEntity.ok(paymentCardMapper.toResponse(paymentCardService.deactivate(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationChecker.canAccessCard(authentication, #id)")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id){
        paymentCardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
