package org.example.restlye1.Service;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ScrapingController {
    private final ScrapingService scrapingService;
    private final AzureAgentService azureAgentService;

    public ScrapingController(ScrapingService scrapingService, AzureAgentService azureAgentService) {
        this.scrapingService = scrapingService;
        this.azureAgentService = azureAgentService;
    }

    @GetMapping(value = "/composicion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getComposicion(@RequestParam String url,
            @RequestParam(required = false) String mockComposition) {
        String composicion;
        if (mockComposition != null && !mockComposition.isEmpty()) {
            composicion = mockComposition;
        } else {
            composicion = scrapingService.analizarCalidad(url);
        }

        if (composicion.startsWith("Error") || composicion.startsWith("No se pudo")) {
            // Return error as JSON
            String errorJson = String.format("{\"error\": \"No se pudo obtener la composición\", \"details\": \"%s\"}",
                    composicion.replace("\"", "\\\""));
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson);
        }

        String analisis = azureAgentService.analizarComposicion(composicion);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(analisis);
    }
}
