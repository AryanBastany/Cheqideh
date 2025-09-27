package cheqideh.dto.request;

import cheqideh.model.account.AccountStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddAccountRequest {
    @NotNull(message = "Account ID cannot be null.")
    private Long accId;

    @NotNull(message = "Balance cannot be null.")
    private BigDecimal balance;
}