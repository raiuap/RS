package org.example.restlye1.Service;

import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.microsoft.playwright.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



@Service
public class ScrapingService {

    public static final Map<String, String> SELECTORES_ESPECIFICOS=Map.of(
            "zara",".product-detail-composition__item"
    );

    private static final List<String> PALABRAS_CLAVE = List.of(
            "algodón", "poliéster", "elastano", "composición", "material", "viscosa", "lana", "acrílico", "rayón"
    );




    public String analizarCalidad(String url) {
        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(false)
            );

            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0 Safari/537.36")
                            .setExtraHTTPHeaders(Map.of(
                                    "Accept-Language", "es-ES,es;q=0.9,en;q=0.8",
                                    "Referer", "https://www.google.com"
                            ))
            );

            Page page = context.newPage();
            page.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            // Navegar a la URL
           /* page.navigate(url);
            page.waitForTimeout(2000);
            System.out.println(page.content());*/

            // Navegar a la URL y esperar que cargue algo de contenido
            page.navigate(url);
            page.waitForTimeout(2000); // espera mínima para cargar JS dinámico

            //Obtener en html renderizado
            String fullHtml = page.content();
            Document document = Jsoup.parse(fullHtml);

            // 1. Intentar selectores específicos por dominio
            String domain = extractDominaClass(url);
            String selector = SELECTORES_ESPECIFICOS.getOrDefault(domain, null);
            if (selector != null) {
                Elements elements = document.select(selector);
                for (Element el : elements) {
                    String text = el.text().toLowerCase();
                    if (contieneMaterial(text)) {
                        browser.close();
                        return text;
                    }
                }
            }

            // 2. buscar en trodo DOM
           Elements allElements = document.getAllElements();
            for (Element el : allElements) {
                String text = el.text().toLowerCase();
                if (contieneMaterial(text)) {
                    browser.close();
                    return text;
                }
            }

            // 3.regex
            String allText = document.text();
            String resultRegex = buscarPorRegex(allText);
            browser.close();
            if (resultRegex != null) {
                return resultRegex;
            }

            return "No se pudo encontrar la composición de la prenda.";


        } catch (Exception e) {
            return "Error al acceder a la URL: " + e.getMessage();
        }

    }



    public String buscarPorRegex(String texto){
        Pattern patron = Pattern.compile("(\\d+%\\s*(algod[oó]n|poli[eé]ster|elastano|lana|acrilico|viscosa|ray[oó]n))", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patron.matcher(texto);
        StringBuilder resultado= new StringBuilder();
        while(matcher.find()){
            resultado.append(matcher.group()).append(" ");
        }
        return resultado.toString().trim();
    }

    public boolean contieneMaterial(String texto){
        return PALABRAS_CLAVE.stream().anyMatch(texto.toLowerCase()::contains);
    }

    public String extractDominaClass(String url){
        try{
        String host= new URL(url).getHost().replace("www.","");
        String dominio= host.split("\\.")[0];
        return dominio;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
