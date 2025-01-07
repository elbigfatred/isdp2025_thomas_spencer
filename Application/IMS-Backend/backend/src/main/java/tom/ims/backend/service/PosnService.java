package tom.ims.backend.service;


import tom.ims.backend.model.Posn;
import tom.ims.backend.repository.PosnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.util.List;

@Service
public class PosnService {

    @Autowired
    private PosnRepository posnRepository;

    public List<Posn> getAllPositions() {
        return posnRepository.findAll();
    }

    public Posn getPositionById(int id) {
        return posnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found!"));
    }
}