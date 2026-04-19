package org.example.restlye1.scraper;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Component;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PlaywrightScraper {

    /**
     * Extrae el contenido renderizado de una URL utilizando Playwright.
     * Aplica optimizaciones de red y gestión estricta de recursos.
     */
    public String getRenderedContent(String url) {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"));

                // Skill: Optimización de red - Bloquear recursos pesados
                context.route("**/*.{png,jpg,jpeg,svg,gif,woff,woff2,ttf,otf}", Route::abort);

                try (Page page = context.newPage()) {
                    // Skill: Evitar detección básica
                    page.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
                    
                    // Configurar timeout de seguridad
                    page.setDefaultTimeout(30000);

                    log.info("Navegando a: {}", url);
                    page.navigate(url);
                    
                    // Skill: Espera resiliente en lugar de Thread.sleep
                    // Esperamos a que el body esté presente al menos
                    page.waitForSelector("body");
                    
                    return page.content();
                }
            }
        } catch (Exception e) {
            log.error("Error crítico durante el scraping de {}: {}", url, e.getMessage());
            throw new RuntimeException("Fallo en la extracción de contenido dinámico", e);
        }
    }
}
