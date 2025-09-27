package cheqideh.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IssueChequeRequest {

    @NotNull(message = "Drawer ID cannot be null.")
    private Long drawerId;

    @NotEmpty(message = "Cheque number cannot be empty.")
    private String number;

    @NotNull(message = "Amount cannot be null.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive.")
    private BigDecimal amount;
}