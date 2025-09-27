package cheqideh.controller;

import cheqideh.dto.request.IssueChequeRequest;
import cheqideh.exception.servicelayer.ChequeBounceException;
import cheqideh.model.cheque.Cheque;
import cheqideh.service.business.ChequeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Map;

@RestController
@RequestMapping("/api/cheques")
@RequiredArgsConstructor
public class ChequeController extends Controller {

    private final ChequeService chequeService;

    @PostMapping
    public ResponseEntity<?> issueCheque(@Valid @RequestBody IssueChequeRequest request) {
        return handleSecureRequest(() -> {
            try {
                Cheque issuedCheque = chequeService.issueCheque(request);

                return ResponseEntity.status(HttpStatus.CREATED).body(issuedCheque);

            } catch (AccountNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
        });
    }

    @PostMapping("/{id}/present")
    public ResponseEntity<?> presentCheque(@PathVariable Long id) {
        return handleSecureRequest(() -> {
            try {
                chequeService.presentCheque(id);
                return ResponseEntity.ok().build();
            } catch (ChequeBounceException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Cheque bounced."));
            } catch (EntityNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
        });
    }
}