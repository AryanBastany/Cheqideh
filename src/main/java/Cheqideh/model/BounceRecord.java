package Cheqideh.model;

import Cheqideh.model.cheque.Cheque;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Setter
@Entity
public class BounceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cheque_id", nullable = false)
    private Cheque cheque;

    private LocalDate bounceDate;
    private String reason;
}
