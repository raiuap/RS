package org.example.restlye1.Domain;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class ScrapingService {

    public static final Map<String, String> SELECTORES_ESPECIFICOS=Map.of(
            "zara",".product-detail-composition__item"
    );

    private static final List<String> PALABRAS_CLAVE = List.of(
            "algodón", "poliéster", "elastano", "composición", "material", "viscosa", "lana", "acrílico", "rayón"
    );




    public String analizarCalidad(String url){
        try{
            Document document = Jsoup.connect(url).userAgent("Mozilla/  5.0").timeout(10*1000).get();
            String domain=extractDominaClass(url);
            String selector= SELECTORES_ESPECIFICOS.getOrDefault(domain ,null);
            if(selector!=null){
                Elements composition=document.select(selector);
                //1.selectores por dominio
                if(!composition.isEmpty()){
                    String text=composition.text();
                    if(contieneMaterial(text)){
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
            String resultRegex=buscarPorRegex(buscarPorRegex(allText));
            if(resultRegex!=null){
                return resultRegex;
            }

            return "No se pudo encontrar la composición de la prenda.";

        }catch(IOException e){
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
        String dominio= host.split("//.")[0];
        return dominio;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
