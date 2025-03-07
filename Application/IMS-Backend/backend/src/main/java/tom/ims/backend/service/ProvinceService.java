package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.Province;
import tom.ims.backend.repository.ProvinceRepository;

import java.util.List;

@Service
public class ProvinceService {

    @Autowired
    private ProvinceRepository provinceRepository;

    public List<Province> getActiveProvinces() {
        return provinceRepository.findByActive((byte) 1);
    }


}