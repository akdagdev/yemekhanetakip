/*
    Scraper class is designed for scraping the Gazi University's meal list webpage.
    It fetches the url, parses it using jsoup library and converts the html table into usable form.
 */
package com.example.yemekhanetakip;

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

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;

        // Resets the process when the document changed
        initialize();
    }

    // We don't want our group members to change the value of "courses" accidentally
    // So there is only a public getter function, no setter
    public HashMap<LocalDate, ArrayList<String>> getCourses() {
        return courses;
    }

    // Constructor
    public Scraper(String url) {

        // Reads the website from internet and sets it as the document
        // This class is designed especially for scraping https://mediko.gazi.edu.tr/view/page/20412
        setDocument(readDocument(url));
    }

    private void initialize(){
        String[][] table = readTableAsArray(findTable(document));

        try {
            String current = null;
            for (int i = 0; i < table.length; i++) {
                for (int j = 0; j < table[i].length; j++) {
                    // In such cases, skip
                    if(table[i][j].toLowerCase().startsWith("kalori") || table[i][j].isBlank()){
                        continue;
                    }

                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(table[i][j]);

                    if(matcher.find()) {
                        // If regular expression detects a number then it is a day title
                        current = table[i][j];
                        courses.put(stringToDate(current), new ArrayList<>());
                    } else {
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

    // Finds the course list as html element from the document and returns it
    private Element findTable(Document document) {
        return document.select("tbody").first();
    }

    // Converts the html table element into a Java array
    private String[][] readTableAsArray(Element table) {
        Elements trs = table.select("tr");
        String[][] data = new String[5][trs.size()];

        HashMap<String, ArrayList<String>> yemekler = new HashMap<>();

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
}
