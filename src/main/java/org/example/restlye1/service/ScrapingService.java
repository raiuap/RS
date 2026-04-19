package org.example.restlye1.service;

import org.example.restlye1.scraper.PlaywrightScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingService {

    private final PlaywrightScraper playwrightScraper;

    public static final Map<String, String> SELECTORES_ESPECIFICOS = Map.of(
            "zara", ".product-detail-composition"
    );

    private static final List<String> PALABRAS_CLAVE = List.of(
            "algodón", "poliéster", "elastano", "composición", "material", "viscosa", "lana", "acrílico", "rayón"
    );

    public String analizarCalidad(String url) {
        try {
            // Delegamos la obtención del HTML dinámico al scraper especializado
            String fullHtml = playwrightScraper.getRenderedContent(url);
            Document document = Jsoup.parse(fullHtml);

            // 1. Intentar selectores específicos por dominio
            String domain = extractDomain(url);
            String selector = SELECTORES_ESPECIFICOS.getOrDefault(domain, null);
            if (selector != null) {
                Elements elements = document.select(selector);
                for (Element el : elements) {
                    String text = el.text().toLowerCase();
                    if (contieneMaterial(text)) {
                        return text;
                    }
                }
            }

            // 2. Buscar por regex en el texto plano del documento
            String allText = document.text();
            String resultRegex = buscarPorRegex(allText);
            if (resultRegex != null && !resultRegex.isEmpty()) {
                return resultRegex;
            }

            return "No se pudo encontrar la composición de la prenda.";

        } catch (Exception e) {
            log.error("Error al analizar calidad para URL {}: {}", url, e.getMessage());
            return "Error al acceder a la URL: " + e.getMessage();
        }
    }

    private String buscarPorRegex(String texto) {
        Pattern patron = Pattern.compile("(\\d+%\\s*(algod[oó]n|poli[eé]ster|elastano|lana|acrilico|viscosa|ray[oó]n))", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patron.matcher(texto);
        StringBuilder resultado = new StringBuilder();
        while (matcher.find()) {
            resultado.append(matcher.group()).append(" ");
        }
        return resultado.toString().trim();
    }

    private boolean contieneMaterial(String texto) {
        return PALABRAS_CLAVE.stream().anyMatch(texto.toLowerCase()::contains);
    }

    private String extractDomain(String url) {
        try {
            String host = new URL(url).getHost().replace("www.", "");
            return host.split("\\.")[0];
        } catch (Exception e) {
            log.warn("No se pudo extraer el dominio de la URL: {}", url);
            return "";
        }
    }
}
