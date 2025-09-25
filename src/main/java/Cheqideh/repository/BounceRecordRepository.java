package Cheqideh.repository;

import Cheqideh.model.BounceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BounceRecordRepository extends JpaRepository<BounceRecord, Long> {
    long countByDrawerIdAndBounceDateAfter(Long drawerId, LocalDate date);
}