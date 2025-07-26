package org.example.restlye1.Domain;

import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;


@Service
public class ScrapingService {
    public String analizarCalidad(String url){
        try{
            //
            Document document = Jsoup.connect(url).userAgent("Mozilla").timeout(10*1000).get();
            Elements composition=document.select("product-detail-composition");



        }catch(IOException e){

        }
    }
}
