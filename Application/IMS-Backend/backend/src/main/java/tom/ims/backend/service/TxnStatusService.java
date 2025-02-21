package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.Employee;
import tom.ims.backend.model.Txnstatus;
import tom.ims.backend.repository.TxnStatusRepository;

import java.util.List;

@Service
public class TxnStatusService {

    @Autowired
    private TxnStatusRepository txnStatusRepository;

    public Txnstatus findByName(String name) {
        // Attempt to find the employee by username
        return txnStatusRepository.findBystatusName(name).orElse(null);
    }

    public List<Txnstatus> findAll() {
        return txnStatusRepository.findAll();
    }
}
