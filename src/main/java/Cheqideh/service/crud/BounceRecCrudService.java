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
        public BounceRecord add(BounceRecord newBounceRec) {
            // TODO: validation
            return bounceRecRepo.save(newBounceRec);
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
            return bounceRecRepo.countByCheque_Drawer_IdAndBounceDateAfter(drawerId, date);
        }
}
