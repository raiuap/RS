package org.example.restlye1.Service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ScrapingController {
    private final ScrapingService scrapingService;

    public ScrapingController(ScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }
    @GetMapping("/composicion")
    public ResponseEntity<String> getComposicion(@RequestParam String url) {
        String resultado =  scrapingService.analizarCalidad(url);
        return ResponseEntity.ok(resultado);
    }
}
