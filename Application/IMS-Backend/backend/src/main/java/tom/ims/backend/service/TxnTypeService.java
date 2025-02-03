package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.Txntype;
import tom.ims.backend.repository.TxnTypeRepository;

@Service
public class TxnTypeService {

    @Autowired
    private TxnTypeRepository txnTypeRepository;

    public Txntype getbyTxnType(String txnType){
        return txnTypeRepository.getBytxnType(txnType).orElse(null);
    }
}
