package tom.ims.backend.service;

import tom.ims.backend.model.Site;
import tom.ims.backend.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteService {

    @Autowired
    private SiteRepository siteRepository;

    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }

    public Site getSiteById(int id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Site not found!"));
    }
}
