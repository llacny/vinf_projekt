package com.company;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.company.Main.doDeserializationAndFormat;

public class PageRank {

    /**
     * Calculate PageRank over a given file, writes a new file with the results
     * @throws Exception
     */
    public void CalculatePR() throws Exception{

        BufferedReader bufferreader = new BufferedReader( new FileReader("articlesV4.txt"));

        String articleTitle = "(.*)#(.*)";
        Pattern pattern = Pattern.compile(articleTitle);
        Map<String, Pair<Double, Double>> values = new HashMap<>();
        Vector<String> titles = new Vector<>();

        String line;
        while( (line = bufferreader.readLine()) != null) {
            Matcher m = pattern.matcher(line);
            m.find();
            String title = m.group(1);
            titles.add(title);
        }

        double titlesSize = titles.size() * 1.0;

        for (String title : titles) {
            values.put(title, new MutablePair<>(1.0/ titlesSize, 0.0));
        }

        String redirectJson = new String(Files.readAllBytes(Path.of("redirectsV4.txt")));
        MultiValueMap redirects = (MultiValueMap) doDeserializationAndFormat(redirectJson);


        for (int i = 0; i < 50; i++) {
            BufferedReader bufferreaderLinks = new BufferedReader( new FileReader("linksV4.txt"));
            String linkLine;
            while ((linkLine = bufferreaderLinks.readLine()) != null) {
                MultiValueMap linkList = (MultiValueMap) doDeserializationAndFormat(linkLine);
                for (Object key : linkList.keySet()) {
                    Pair<Double, Double> myValues = values.get(key);
                    Double myPrev = myValues.getLeft();

                    Collection<String> edges = linkList.getCollection(key);

                    //counting of valid links
                    int validLinks = 0;
                    for (String edge : edges) {
                        if (values.containsKey(edge) && edge != key) {
                            validLinks++;
                        } else {
                            if (redirects.containsKey(edge) && edge != key)
                                validLinks++;
                        }
                    }

                    for (String edge : edges) {
                        if (values.containsKey(edge) && edge != key) {
                            Pair<Double, Double> rankValues = values.get(edge);
                            Double current = rankValues.getRight() + myPrev / validLinks;

                            Pair<Double, Double> newValues = new MutablePair<>(rankValues.getLeft(), current);
                            values.put(edge, newValues);
                        } else {
                            if (edge == key)
                                continue;

                            //if such link doesnt exist - lookup in redirects, if not even in redirects - skip
                            if (redirects.containsKey(edge)) {
                                var redirectValue = redirects.getCollection(edge);
                                String redirectLink = (String) redirectValue.toArray()[0];

                                if (redirectLink.contains("#")) {
                                    Matcher m = pattern.matcher(redirectLink);
                                    m.find();
                                    String realLink = m.group(1);
                                    if (values.containsKey(realLink)) {
                                        Pair<Double, Double> rankValues = values.get(realLink);
                                        Double current = rankValues.getRight() + myPrev / validLinks;

                                        Pair<Double, Double> newValues = new MutablePair<>(rankValues.getLeft(), current);
                                        values.put(realLink, newValues);
                                    }
                                } else {
                                    if (values.containsKey(redirectLink)) {
                                        Pair<Double, Double> rankValues = values.get(redirectLink);
                                        Double current = rankValues.getRight() + myPrev / validLinks;

                                        Pair<Double, Double> newValues = new MutablePair<>(rankValues.getLeft(), current);
                                        values.put(redirectLink, newValues);
                                    }
                                }

                            }
                        }

                    }
                }
            }

            //swap new and old values

            for (String title : titles) {
                Pair<Double, Double> myValues = values.get(title);
                Double newPrev = myValues.getRight();
                values.put(title, new MutablePair<>(newPrev, 0.0));
            }
        }
        FileWriter fileWriter = new FileWriter("result-swap.txt");

        values.forEach((k, v) -> {
            try {
                fileWriter.write((k + "\t" + v.getLeft()));
                fileWriter.write(System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        fileWriter.close();
    }
}
