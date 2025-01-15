package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
