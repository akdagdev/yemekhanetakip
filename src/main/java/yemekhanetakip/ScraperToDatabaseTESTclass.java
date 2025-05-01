package yemekhanetakip;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

public class ScraperToDatabaseTESTclass {
    public static void main(String[] args) {
        Scraper scraper = new Scraper("https://mediko.gazi.edu.tr/view/page/20412");

        HashSet<String> yemekSeti = new HashSet<>();

        for (LocalDate tarih : scraper.getCourses().keySet()) {
            ArrayList<String> yemekler = scraper.getCourses().get(tarih);

            for (String yemek : yemekler) {
                if (yemek != null && !yemek.isBlank()) {


                    if (yemek.contains("* "))
                    {
                        yemek = yemek.replace("* ", "");

                    }
                    else if (yemek.contains("*"))
                    {
                         yemek = yemek.replace("*", "");

                    }


                    yemekSeti.add(yemek);
                }
            }
        }

        for (String yemek : yemekSeti) {
            DatabaseManager.insertMealIfNotExists(yemek);
        }

        System.out.println("Scraper'dan gelen temizlenmiş yemekler veritabanına işlendi.");
    }
}
