package cheqideh.controller;

import cheqideh.dto.request.AddAccountRequest;
import cheqideh.model.account.Account;
import cheqideh.service.business.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController extends Controller{
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> issueCheque(@Valid @RequestBody AddAccountRequest request) {
        return handleSecureRequest(() -> {
            try {
                Account newAccount = accountService.addAccount(request);

                return ResponseEntity.status(HttpStatus.CREATED).body(newAccount);

            } catch (AccountNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
        });
    }
}
