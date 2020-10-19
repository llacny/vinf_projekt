package com.company;


import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.time.StopWatch;
import org.json.simple.JSONObject;
import java.io.*;
import java.util.Map;


public class Main {

    public static void main(String[] args) throws Exception {

        File wiki_input_file = new File("skwiki-latest-pages-articles.xml.bz2");

        StopWatch watch = new StopWatch();
        watch.start();

        var parseResult = new WikiParser().getTitleToIdMatching(wiki_input_file);
        Map<String, String> pageTitleIds = parseResult.getLeft();
        MultiValueMap redirects = parseResult.getRight();

        watch.stop();
        System.out.println("Time elapsed: " + watch.getTime());
        //System.out.println("Article count: " + counter);
        System.out.println("Articles in ns 0 count: " + pageTitleIds.size());

        watch.reset();

        watch.start();
        System.out.println("Starting to write articles");
        FileWriter fileWriter = new FileWriter("articles.txt");
        JSONObject json = new JSONObject(pageTitleIds);
        System.out.println("Writing to file " + watch.getTime());
        fileWriter.write(json.toJSONString());
        fileWriter.close();

        watch.stop();
        System.out.println("Done writing articles to file " + watch.getTime());

        watch.reset();

        watch.start();
        System.out.println("Starting to write redirects");
        FileWriter fileWriterRedirects = new FileWriter("redirects.txt");
        JSONObject jsonRedirects = new JSONObject(redirects);
        System.out.println("Writing to file " + watch.getTime());
        fileWriterRedirects.write(jsonRedirects.toJSONString());
        fileWriterRedirects.close();

        watch.stop();
        System.out.println("Done writing redirects to file " + watch.getTime());
        System.out.println("Redirect count: " + redirects.totalSize());


    }
}
