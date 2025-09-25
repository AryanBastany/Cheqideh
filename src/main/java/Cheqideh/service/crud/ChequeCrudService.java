package Cheqideh.service.crud;

import Cheqideh.model.cheque.Cheque;
import Cheqideh.repository.ChequeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChequeCrudService implements CrudService<Cheque, Long>{
        private final ChequeRepository chequeRepo;

        @Override
        public void add(Cheque newCheque) {
            // TODO: validation
            chequeRepo.save(newCheque);
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

