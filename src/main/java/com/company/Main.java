package com.company;


import org.apache.commons.lang3.time.StopWatch;
import org.json.simple.JSONObject;
import java.io.*;
import java.util.Map;


public class Main {

    public static void main(String[] args) throws Exception {

        File wiki_input_file = new File("skwiki-latest-pages-articles.xml.bz2");

        StopWatch watch = new StopWatch();
        watch.start();

        Map<String, String> pageTitleIds = new WikiParser().getTitleTOIdMatching(wiki_input_file);

        watch.stop();
        System.out.println("Time elapsed: " + watch.getTime());
        //System.out.println("Article count: " + counter);
        System.out.println("Articles in ns 0 count: " + pageTitleIds.size());

        FileWriter fileWriter = new FileWriter("articles.txt");
        JSONObject json = new JSONObject(pageTitleIds);
        fileWriter.write(json.toJSONString());
        fileWriter.close();
    }
}
