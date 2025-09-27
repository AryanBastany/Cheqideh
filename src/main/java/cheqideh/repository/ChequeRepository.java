package cheqideh.repository;

import cheqideh.model.cheque.Cheque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChequeRepository extends JpaRepository<Cheque, Long>{

}
