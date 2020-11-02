package com.company;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.company.Main.doDeserializationAndFormat;

public class PageRank {

    public void CalculatePR() throws Exception{

        BufferedReader bufferreader = new BufferedReader( new FileReader("articlesV2.txt"));

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

        BufferedReader bufferreaderLinks = new BufferedReader( new FileReader("links.txt"));

        String linkLine;
        while( (linkLine = bufferreaderLinks.readLine()) != null) {
            MultiValueMap linkList = (MultiValueMap) doDeserializationAndFormat(linkLine);
            for(Object key : linkList.keySet())
            {
                Pair<Double,Double> myValues = values.get(key);
                Double myPrev = myValues.getLeft();

                Collection<String> edges = linkList.getCollection(key);

                for(String edge : edges)
                {
                    if(values.containsKey(edge)){
                        Pair<Double,Double> rankValues = values.get(edge);
                        Double current = rankValues.getRight() + myPrev / edges.size();

                        Pair<Double,Double> newValues = new MutablePair<>(rankValues.getLeft(),current);
                        values.put(edge,newValues);
                    }
                    else {
                        //ak taky link neexistuje - pozrieť sa v redirectoch, ak ani v redirectoch tak nepripocitat
                    }

                }
            }
        }

        FileWriter fileWriter = new FileWriter("PRresults.txt");

        values.forEach((k, v) -> {
            try {
                fileWriter.write((k + " = " + v.getRight()));
                fileWriter.write(System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        fileWriter.close();
    }
}
