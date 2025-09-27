package cheqideh.repository;

import cheqideh.model.BounceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BounceRecordRepository extends JpaRepository<BounceRecord, Long> {
    long countByCheque_Drawer_IdAndBounceDateAfter(Long drawerId, LocalDate date);
}