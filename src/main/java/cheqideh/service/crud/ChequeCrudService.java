package cheqideh.service.crud;

import cheqideh.model.cheque.Cheque;
import cheqideh.repository.ChequeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChequeCrudService implements CrudService<Cheque, Long>{
        private final ChequeRepository chequeRepo;

        @Override
        public Cheque add(Cheque newCheque) {
            // TODO: validation
           return chequeRepo.save(newCheque);
        }

        @Override
        public void removeById(Long id) {
            // TODO: validation
            chequeRepo.findById(id).ifPresent(chequeRepo::delete);
        }

        @Override
        public Cheque findById(Long id) {
            // TODO: validation
            return chequeRepo.findById(id).orElse(null);
        }
}

