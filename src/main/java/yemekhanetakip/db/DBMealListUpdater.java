package yemekhanetakip.db;

import yemekhanetakip.scraper.Scraper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

public class DBMealListUpdater {

    // Singleton design pattern
    private static DBMealListUpdater instance;
    public static DBMealListUpdater getInstance()
    {
        if (instance == null)
        {
            instance = new DBMealListUpdater();
        }
        return instance;
    }

    // Adds each meal to the database if it doesn't exist yet
    public void updateList(Scraper scraper) {
        HashSet<String> yemekSeti = new HashSet<>();

        for (LocalDate tarih : scraper.getCourses().keySet()) {
            ArrayList<String> yemekler = scraper.getCourses().get(tarih);

            for (String yemek : yemekler) {
                if (yemek != null && !yemek.isBlank()) {
                    yemekSeti.add(yemek);
                }
            }
        }

        MealDBManager dbManager = MealDBManager.getInstance();
        for (String yemek : yemekSeti) {
            dbManager.insertMealIfNotExists(yemek);
        }

        System.out.println("Scraper'dan gelen temizlenmiş yemekler veritabanına işlendi.");
    }
}
