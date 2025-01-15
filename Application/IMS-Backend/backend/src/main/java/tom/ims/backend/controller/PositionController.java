package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tom.ims.backend.model.Posn;
import tom.ims.backend.service.PosnService;

import javax.swing.text.Position;
import java.util.List;

@RestController
@RequestMapping("/api/positions")
public class PositionController {


    @Autowired
    private PosnService positionService;

    @GetMapping
    public List<Posn> getAllPositions() {
        // Fetch positions from the service
        return positionService.getAllPositions();
    }
}
