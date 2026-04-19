package org.example.restlye1.controller;

import org.example.restlye1.service.AzureAgentService;
import org.example.restlye1.service.ScrapingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/scraping")
@RequiredArgsConstructor
public class ScrapingController {

    private final ScrapingService scrapingService;
    private final AzureAgentService azureAgentService;

    @GetMapping(value = "/composicion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getComposicion(
            @RequestParam String url,
            @RequestParam(required = false) String mockComposition) {
        
        log.info("Recibida petición de análisis para URL: {}", url);
        
        String composicion;
        if (mockComposition != null && !mockComposition.isEmpty()) {
            composicion = mockComposition;
        } else {
            composicion = scrapingService.analizarCalidad(url);
        }

        if (composicion.startsWith("Error") || composicion.startsWith("No se pudo")) {
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

    /**
     * Endpoint temporal para validación de conectividad y entorno (Workflow Progresivo).
     */
    @GetMapping("/test-connectivity")
    public ResponseEntity<String> testConnectivity(@RequestParam(defaultValue = "https://www.google.com") String url) {
        try {
            log.info("Validando conectividad de Playwright con URL: {}", url);
            String content = scrapingService.analizarCalidad(url);
            return ResponseEntity.ok("Conectividad OK. Contenido extraído o validado: " + content);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error de conectividad: " + e.getMessage());
        }
    }
}
