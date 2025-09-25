package Cheqideh.repository;

import Cheqideh.model.account.Account;
import Cheqideh.model.cheque.Cheque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChequeRepository extends JpaRepository<Cheque, Long>{

}
