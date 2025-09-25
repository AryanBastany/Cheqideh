package Cheqideh.model.account;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal balance;

    private AccountStatus status;
}
