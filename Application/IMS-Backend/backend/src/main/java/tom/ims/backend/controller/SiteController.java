package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.Site;
import tom.ims.backend.service.SiteService;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    @Autowired
    private SiteService siteService;

    @GetMapping
    public List<Site> getAllSites() {
        return siteService.getAllSites();
    }

    // ✅ Add a new site
    @PostMapping("/add")
    public ResponseEntity<Site> addSite(@RequestBody Site site) {
        try {
            Site newSite = siteService.saveSite(site);
            return ResponseEntity.ok(newSite);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Edit an existing site
    @PutMapping("/edit/{id}")
    public ResponseEntity<Site> updateSite(@PathVariable Integer id, @RequestBody Site updatedSite) {
        try {
            Site site = siteService.updateSite(id, updatedSite);
            return site != null ? ResponseEntity.ok(site) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
