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
        //new WikiParser().getTitleToIdMatching(wiki_input_file);
        //new WikiParser().parseLinks(wiki_input_file);


        //FileReader fileReader = new FileReader("redirects.txt");
        //String json = new String(Files.readAllBytes(Path.of("redirects.txt")));
        //MultiValueMap redirects = (MultiValueMap) doDeserializationAndFormat(json);
        //System.out.println(redirects.size());
    }

    /**
     * Serializes a given JSON string into a MultiMap
     * @param serializedString input JSON string
     * @return  serialized MultiMap
     * @throws Exception
     */
    public static MultiMap doDeserialization(String serializedString) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        Class<MultiValueMap> classz = MultiValueMap.class;
        MultiMap map = mapper.readValue(serializedString, classz);
        return (MultiMap) map;
    }


    /**
     * Serializes a given JSON string into a MultiMap and formats it correctly
     * @param serializedString input JSON string
     * @return  serialized and correctly formated MultiMap
     * @throws Exception
     */
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
