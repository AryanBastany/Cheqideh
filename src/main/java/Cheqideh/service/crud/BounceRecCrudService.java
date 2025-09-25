package Cheqideh.service.crud;

import Cheqideh.model.BounceRecord;
import Cheqideh.repository.BounceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BounceRecCrudService implements CrudService<BounceRecord, Long>{
        private final BounceRecordRepository bounceRecRepo;

        @Override
        public void add(BounceRecord newBounceRec) {
            // TODO: validation
            bounceRecRepo.save(newBounceRec);
        }

        @Override
        public void removeById(Long id) {
            // TODO: validation
            bounceRecRepo.findById(id).ifPresent(bounceRecRepo::delete);
        }

        @Override
        public BounceRecord findById(Long id) {
            // TODO: validation
            return bounceRecRepo.findById(id).orElse(null);
        }

        public long countBounceDateAfter(Long drawerId, LocalDate date) {
            return bounceRecRepo.countByDrawerIdAndBounceDateAfter(drawerId, date);
        }
}
