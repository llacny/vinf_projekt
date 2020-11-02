package com.company;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.time.StopWatch;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {


    public static void main(String[] args) throws Exception {

        new PageRank().CalculatePR();
        //File wiki_input_file = new File("skwiki-latest-pages-articles.xml.bz2");
        //new WikiParser().parseLinks(wiki_input_file);

        //parseIDsAndRedirects();

        //FileReader fileReader = new FileReader("redirects.txt");
        //String json = new String(Files.readAllBytes(Path.of("redirects.txt")));
        //MultiValueMap redirects = (MultiValueMap) doDeserializationAndFormat(json);
        //System.out.println(redirects.size());
    }

    public static void parseIDsAndRedirects() throws Exception
    {
        File wiki_input_file = new File("skwiki-latest-pages-articles.xml.bz2");

        StopWatch watch = new StopWatch();
        watch.start();

        new WikiParser().getTitleToIdMatching(wiki_input_file);
        //var parseResult = new WikiParser().getTitleToIdMatching(wiki_input_file);
        //Map<String, String> pageTitleIds = parseResult.getLeft();
        //MultiValueMap redirects = parseResult.getRight();

        watch.stop();
        System.out.println("Time elapsed: " + watch.getTime());
        //System.out.println("Article count: " + counter);
        //System.out.println("Articles in ns 0 count: " + pageTitleIds.size());

        watch.reset();

        watch.start();
        System.out.println("Starting to write articles");
        //FileWriter fileWriter = new FileWriter("articles.txt");
        //JSONObject json = new JSONObject(pageTitleIds);
        System.out.println("Writing to file " + watch.getTime());
        //fileWriter.write(json.toJSONString());
        //fileWriter.close();

        watch.stop();
        System.out.println("Done writing articles to file " + watch.getTime());

        watch.reset();

        watch.start();
        System.out.println("Starting to write redirects");
        //FileWriter fileWriterRedirects = new FileWriter("redirects.txt");
        //JSONObject jsonRedirects = new JSONObject(redirects);
        System.out.println("Writing to file " + watch.getTime());
        //fileWriterRedirects.write(jsonRedirects.toJSONString());
        //ileWriterRedirects.close();

        watch.stop();
        System.out.println("Done writing redirects to file " + watch.getTime());
        //System.out.println("Redirect count: " + redirects.totalSize());
    }

    public static MultiMap doDeserialization(String serializedString) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        Class<MultiValueMap> classz = MultiValueMap.class;
        MultiMap map = mapper.readValue(serializedString, classz);
        return (MultiMap) map;
    }

    public static MultiMap doDeserializationAndFormat(String serializedString) throws Exception {
        MultiMap source = doDeserialization(serializedString);
        MultiMap result  =  new MultiValueMap();
        for (Object key: source.keySet()) {


            List allValues = (List)source.get(key);
            Iterator iter = allValues.iterator();

            while (iter.hasNext()) {
                List<String> datas = (List<String>)iter.next();

                for (String s: datas) {
                    result.put(key, s);
                }
            }

        }
        return result;
    }

}
