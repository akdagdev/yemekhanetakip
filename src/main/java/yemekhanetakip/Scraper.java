/*
    Scraper class is designed for scraping the Gazi University's meal list webpage.
    It fetches the url, parses it using jsoup library and converts the html table into usable form.
 */
package yemekhanetakip;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Scraper {
    private Document document;
    private final HashMap<LocalDate, ArrayList<String>> courses = new HashMap<>();
    private final HashMap<LocalDate, String> calories = new HashMap<>();

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;

        // Resets the process when the document changed
        initialize();
    }

    // We don't want our group members to change the value of "courses" accidentally,
    // So there is only a public getter function, no setter
    public HashMap<LocalDate, ArrayList<String>> getCourses() {
        return courses;
    }

    // Getter for calories
    public HashMap<LocalDate, String> getCalories() {
        return calories;
    }

    // Constructor
    public Scraper(String url) {

        // Reads the website from the internet and sets it as the document
        // This class is designed especially for scraping https://mediko.gazi.edu.tr/view/page/20412
        setDocument(readDocument(url));
    }

    private void initialize(){
        String[][] table = readTableAsArray(findTable(document));

        try {
            String current = null;
            for (int i = 0; i < table.length; i++) {
                for (int j = 0; j < table[i].length; j++) {
                    // Skip empty entries
                    if(table[i][j].isBlank()){
                        continue;
                    }

                    // Capture calorie information
                    if(table[i][j].toLowerCase().startsWith("kalori")) {
                        if(current != null) {
                            calories.put(stringToDate(current), table[i][j]);
                        }
                        continue;
                    }

                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(table[i][j]);

                    if(matcher.find()) {
                        // If a regular expression detects a number, then it is a day title
                        current = table[i][j];
                        courses.put(stringToDate(current), new ArrayList<>());
                    } else {
                        if (table[i][j].contains("* "))
                            table[i][j] = table[i][j].replace("* ", "*");

                        courses.get(stringToDate(current)).add(table[i][j]);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        for (LocalDate key : courses.keySet()) {
            System.out.println(key);
            System.out.println(courses.get(key));
            if (calories.containsKey(key)) {
                System.out.println(calories.get(key));
            }
        }
    }

    // Fetches the desired url as html
    private Document readDocument(String url) {
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return doc;
    }

    // Finds the course list as HTML element from the document and returns it
    private Element findTable(Document document) {
        return document.select("tbody").first();
    }

    // Converts the HTML table element into a Java array
    private String[][] readTableAsArray(Element table) {
        Elements trs = table.select("tr");
        String[][] data = new String[5][trs.size()];

        for (int i = 0; i < trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            for (int j = 0; j < tds.size(); j++) {
                data[j][i] = tds.get(j).text().trim();
            }
        }

        return data;
    }

    // Converts Turkish local date text such as "25 Nisan 2025 Cuma" into LocalDate
    private LocalDate stringToDate(String text){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy EEEE", Locale.of("tr", "TR"));

        return LocalDate.parse(text, formatter);
    }

    // Returns the menu for a specific date in the format expected by the controller
    public String getMenuForDate(String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        ArrayList<String> menuItems = courses.get(date);
        
        if (menuItems == null || menuItems.isEmpty()) {
            return "[]\n" + date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }
        
        // Format the menu items as a string
        StringBuilder menuBuilder = new StringBuilder("[");
        for (int i = 0; i < menuItems.size(); i++) {
            menuBuilder.append(menuItems.get(i));
            if (i < menuItems.size() - 1) {
                menuBuilder.append(", ");
            }
        }
        menuBuilder.append("]\n").append(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        
        return menuBuilder.toString();
    }
    
    // Returns the calorie information for a specific date
    public String getCaloriesForDate(String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        String calorieInfo = calories.get(date);
        
        if (calorieInfo == null) {
            return "Bilgi yok";
        }
        
        return calorieInfo;
    }
}
