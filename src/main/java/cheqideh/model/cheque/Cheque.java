package cheqideh.model.cheque;

import cheqideh.model.account.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@Setter
@Getter
@Entity
public class Cheque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drawer_id", nullable = false)
    private Account drawer;

    private BigDecimal amount;
    private LocalDate issueDate;
    private ChequeStatus status;
}