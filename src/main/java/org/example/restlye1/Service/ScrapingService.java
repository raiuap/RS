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

    public String analizarCalidadSs(String url) {
            try {
                // Hacemos la petición con Jsoup
                Connection.Response response = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .ignoreContentType(true) // Para que acepte JSON como respuesta
                        .method(Connection.Method.GET)
                        .execute();

                // Obtenemos el JSON como String
                String json = response.body();
                System.out.println(json);
                return json;

            } catch (Exception e) {
                System.out.println("Error en ZaraScraper: " + e.getMessage());
                return "Error en ZaraScraper: " + e.getMessage();
            }
        }



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
            page.navigate(url);
            page.waitForTimeout(2000);
            System.out.println(page.content());

            // Simular scroll humano
            Random random = new Random();
            page.mouse().wheel(0, 200 + random.nextInt(500));
            page.waitForTimeout(1000 + random.nextInt(2000));
            page.mouse().wheel(0, 500 + random.nextInt(500));
            page.waitForTimeout(1500);


            Document document = Jsoup.parse(page.content());

            String domain=extractDominaClass(url);
            String selector= SELECTORES_ESPECIFICOS.getOrDefault(domain ,null);

            if(selector!=null){
                Elements composition= document.select(selector);
                if(!composition.isEmpty()){
                    String text=composition.text();
                    if(contieneMaterial(text)) {
                        System.out.println(text);
                        return text;
                    }
                }
            }
            //2.Busqueda por texto en all los elementos
            Elements all=document.getAllElements();
            for(Element el: all){
                String text=el.text().toLowerCase();
                if(contieneMaterial(text)){
                    return text;
                }
            }
            //3.Regex sobre el texto completo del documento
            String allText= document.text();
            String resultRegex=buscarPorRegex(allText   );
            if(resultRegex!=null){
                return resultRegex;
            }
            browser.close();

            return "No se pudo encontrar la composición de la prenda.";

        } catch (Exception e) {
            return "Error al acceder a la URL: " + e.getMessage();
        }

    }







    public String analizarCalidadA(String url){
        try{
            Random random = new Random();
            int delay=200+ random.nextInt(3000);
            Thread.sleep(delay);
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .referrer("https://www.google.com")
                    .timeout(10000)  // Tiempo máximo en milisegundos
                    .get();
            System.out.println(document.html());



            /*String domain=extractDominaClass(url);
            String selector= SELECTORES_ESPECIFICOS.getOrDefault(domain ,null);
            if(selector!=null){
                Elements composition=document.select(selector);
                //1.selectores por dominio
                if(!composition.isEmpty()){
                    String text=composition.text();
                    if(contieneMaterial(text)){
                        System.out.println(text);
                        return text;
                    }
                }
            }
            //2.Busqueda por texto en all los elementos
            Elements all=document.getAllElements();
            for(Element el: all){
                String text=el.text().toLowerCase();
                if(contieneMaterial(text)){
                    return text;
                }
            }
            //3.Regex sobre el texto completo del documento
            String allText= document.text();
            String resultRegex=buscarPorRegex(allText   );
            if(resultRegex!=null){
                return resultRegex;
            }*/

            return "No se pudo encontrar la composición de la prenda.";

        }catch(IOException e){
            return "Error al acceder a la URL: " + e.getMessage();

        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "El proceso fue interrumpido";
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
        String dominio= host.split("//.")[0];
        return dominio;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
