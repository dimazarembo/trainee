package by.dzarembo.trainee.controller;

import by.dzarembo.trainee.dto.PaymentCardCreateRequest;
import by.dzarembo.trainee.dto.PaymentCardResponse;
import by.dzarembo.trainee.dto.PaymentCardUpdateRequest;
import by.dzarembo.trainee.mapper.PaymentCardMapper;
import by.dzarembo.trainee.security.AccessChecker;
import by.dzarembo.trainee.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
@AllArgsConstructor
public class PaymentCardController {
    private final PaymentCardService paymentCardService;
    private final PaymentCardMapper paymentCardMapper;
    private final AccessChecker accessChecker;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponse> getPaymentCard(@PathVariable Long id) {
        accessChecker.checkCardAccess(id);
        return ResponseEntity.ok(paymentCardMapper.toResponse(paymentCardService.getById(id)));
    }

    @GetMapping
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
    public ResponseEntity<PaymentCardResponse> createPaymentCard (@Valid @RequestBody PaymentCardCreateRequest cardCreateRequest){
        accessChecker.checkUserAccess(cardCreateRequest.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).
                body(paymentCardMapper.toResponse(paymentCardService.create(cardCreateRequest.getUserId(), paymentCardMapper.toEntity(cardCreateRequest))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardResponse>  updatePaymentCard (@PathVariable Long id, @Valid @RequestBody PaymentCardUpdateRequest cardUpdateRequest){
        accessChecker.checkCardAccess(id);
        return ResponseEntity.ok(paymentCardMapper.toResponse(paymentCardService.update(id, paymentCardMapper.toEntity(cardUpdateRequest))));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PaymentCardResponse> activatePaymentCard(@PathVariable Long id){
        accessChecker.checkCardAccess(id);
        return ResponseEntity.ok(paymentCardMapper.toResponse(paymentCardService.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PaymentCardResponse> deactivatePaymentCard(@PathVariable Long id){
        accessChecker.checkCardAccess(id);
        return ResponseEntity.ok(paymentCardMapper.toResponse(paymentCardService.deactivate(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id){
        accessChecker.checkCardAccess(id);
        paymentCardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
