package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.Province;
import tom.ims.backend.service.ProvinceService;

import java.util.List;

@RestController
@RequestMapping("/api/provinces")
public class ProvinceController {

    @Autowired
    private ProvinceService provinceService;

    @GetMapping("/active")
    public List<Province> getActiveProvinces() {
        return provinceService.getActiveProvinces();
    }
}