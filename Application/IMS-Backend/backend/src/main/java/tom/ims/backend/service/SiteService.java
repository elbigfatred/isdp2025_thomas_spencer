package tom.ims.backend.service;

import tom.ims.backend.model.Site;
import tom.ims.backend.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Site saveSite(Site site) {
        return siteRepository.save(site);
    }

    public Site updateSite(Integer id, Site updatedSite) {
        Optional<Site> existingSiteOpt = siteRepository.findById(id);
        if (existingSiteOpt.isPresent()) {
            Site existingSite = existingSiteOpt.get();
            existingSite.setSiteName(updatedSite.getSiteName());
            existingSite.setAddress(updatedSite.getAddress());
            existingSite.setAddress2(updatedSite.getAddress2());
            existingSite.setCity(updatedSite.getCity());
            existingSite.setProvinceID(updatedSite.getProvinceID());
            existingSite.setCountry(updatedSite.getCountry());
            existingSite.setPostalCode(updatedSite.getPostalCode());
            existingSite.setPhone(updatedSite.getPhone());
            existingSite.setDayOfWeek(updatedSite.getDayOfWeek());
            existingSite.setDistanceFromWH(updatedSite.getDistanceFromWH());
            existingSite.setNotes(updatedSite.getNotes());
            existingSite.setActive(updatedSite.getActive());

            return siteRepository.save(existingSite);
        } else {
            return null; // Not found
        }
    }
}
