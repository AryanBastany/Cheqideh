package Cheqideh.service.crud;

import Cheqideh.model.BounceRecord;
import Cheqideh.repository.BounceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BounceRecordService implements CrudService<BounceRecord, Long>{
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
}
