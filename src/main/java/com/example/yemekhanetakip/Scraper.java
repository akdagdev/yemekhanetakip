package com.example.yemekhanetakip;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Scraper {
    private Document document;
    private HashMap<String, ArrayList<String>> courses = new HashMap<>();

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
    public HashMap<String, ArrayList<String>> getCourses() {
        return courses;
    }

    // Constructor
    public Scraper(String url) {

        // Reads the website from internet and sets it as the document
        // This class is designed especially for scraping https://mediko.gazi.edu.tr/view/page/20412
        setDocument(readDocument(url));
    }

    private void initialize(){
        String[][] data = readTableAsArray(findTable(document));

        try {
            String current = null;
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    if(data[i][j].toLowerCase().startsWith("kalori"))
                        continue;

                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(data[i][j]);

                    if(matcher.find()) {
                        // If regular expression detects a number then it is a day title
                        current = data[i][j];
                        courses.put(current, new ArrayList<>());
                    } else {
                        courses.get(current).add(data[i][j]);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        for (String key : courses.keySet()) {
            System.out.println(key);
            System.out.println(courses.get(key));
        }

    }

    private Document readDocument(String url) {
        Document doc;

        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return doc;
    }

    // Finds the course list as html element from the document and returns it
    private Element findTable(Document document) {
        return document.select("tbody").first();
    }


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
}
