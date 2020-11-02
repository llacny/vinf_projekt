package com.company;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.company.Main.doDeserializationAndFormat;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class WikiParser {

    private final String title = "<title>(.*)</title>";
    private final Pattern titlePattern = Pattern.compile(title);

    private final String ns = "<ns>(.*)</ns>";
    private final Pattern nsPattern = Pattern.compile(ns);

    private final String id = "<id>(.*)</id>";
    private final Pattern idPattern = Pattern.compile(id);

    private final String pageStart = "<page>";
    private final Pattern pageStartPattern = Pattern.compile(pageStart);

    private final String pageEnd = "</page>";
    private final Pattern pageEndPattern = Pattern.compile(pageEnd);

    private final String redirect = "<text.*>#(REDIRECT|presmeruj) \\[\\[(.*)\\]\\]</text>";
    private final  Pattern redirectPattern = Pattern.compile(redirect);

    private final String link = "\\[\\[([^\\|\\[\\]\\#\\{\\}]*)\\|?([^\\|\\[\\]\\#\\{\\}]*)\\]\\]";
    private final  Pattern linkPattern = Pattern.compile(link);

    private final Map<String,String> titleToId = new HashMap<>();
    private final MultiValueMap redirects = new MultiValueMap();

    public void getTitleToIdMatching(File file) throws Exception {

        //FileReader fileReader = new FileReader("redirects.txt");
        //String json = new String(Files.readAllBytes(Path.of("redirects.txt")));
        //MultiValueMap redirects = (MultiValueMap) doDeserializationAndFormat(json);

        //FileWriter fileWriterArticles = new FileWriter("articlesV2.txt");
        FileWriter fileWriterRedirects = new FileWriter("redirectsV2.txt");

        InputStream fileStream = new FileInputStream(file);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(fileStream);
        Reader decoder = new InputStreamReader(bzIn, UTF_8);
        BufferedReader bufferedReader = new BufferedReader(decoder);

        boolean pageFound = false;
        boolean titleFound = false;
        boolean idFound = false;
        boolean nsFound = false;
        boolean redirectFound = false;
        //int counter = 0;

            //for(int i = 0; i < 10000; i++) {
            String line;

            String title = "";
            String id = "";
            int ns = -1;

            while( (line = bufferedReader.readLine()) != null) {
                //for(int i = 0; i < 1000000; i++){
                //    line = bufferedReader.readLine();

                if(!pageFound && !findPageStart(line))
                    continue;
                else
                    pageFound = true;

                if(!titleFound) {
                    title = findTitle(line);
                    if (title == null)
                        continue;
                    else {
                        //counter++;
                        titleFound = true;
                        continue;
                    }
                }

                if(!nsFound){
                    ns = findNs(line);
                    if (ns == -1)
                        continue;
                    else{
                        nsFound = true;
                        continue;
                    }
                }

                if(!idFound){
                    id = findID(line);
                    if (id == null)
                        continue;
                    else {
                        idFound = true;
                        continue;
                    }
                }

                //this block is used to find redirects
                String redirect;
                if( !redirectFound && ((redirect = detectRedirect(line)) != null))
                {
                    //System.out.println("redirect found");
                    if(ns == 0)
                        redirects.put(title,redirect);
                    redirectFound = true;
                    continue;
                }

                if(findPageEnd(line)){

                    //counter++;

                    if(ns == 0) {
                        if(!redirects.containsKey(title)) {
                            //fileWriterArticles.write(title + "#" + id);
                            //fileWriterArticles.write(System.lineSeparator());
                        }
                    }

                    pageFound = false;
                    titleFound = false;
                    title = "";
                    idFound = false;
                    id = "";
                    nsFound = false;
                    ns = -1;
                    redirectFound = false;
                }
            }

        JSONObject jsonRedirects = new JSONObject(redirects);
        fileWriterRedirects.write(jsonRedirects.toJSONString());
        fileWriterRedirects.close();
    }

    public void parseLinks(File file) throws Exception
    {
        String json = new String(Files.readAllBytes(Path.of("redirectsV2.txt")));
        MultiValueMap redirects = (MultiValueMap) doDeserializationAndFormat(json);

        FileWriter fileWriter = new FileWriter("links.txt");

        InputStream fileStream = new FileInputStream(file);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(fileStream);
        Reader decoder = new InputStreamReader(bzIn, UTF_8);
        BufferedReader bufferedReader = new BufferedReader(decoder);

        boolean pageFound = false;
        boolean titleFound = false;
        boolean idFound = false;
        boolean nsFound = false;
        //int counter = 0;

        //for(int i = 0; i < 10000; i++) {
        String line;

        String title = "";
        String id = "";
        int ns = -1;
        MultiValueMap links = new MultiValueMap();

        while( (line = bufferedReader.readLine()) != null) {
        //for(int i = 0; i < 10000; i++){
            //line = bufferedReader.readLine();

            if(!pageFound && !findPageStart(line))
                continue;
            else
                pageFound = true;

            if(!titleFound) {
                title = findTitle(line);
                if (title == null)
                    continue;
                else {
                    //counter++;
                    titleFound = true;
                    continue;
                }
            }

            if(!nsFound){
                ns = findNs(line);
                if (ns == -1)
                    continue;
                else{
                    nsFound = true;
                    continue;
                }
            }

            if(!idFound){
                id = findID(line);
                if (id == null)
                    continue;
                else {
                    idFound = true;
                    continue;
                }
            }

            if(ns == 0) {
                Matcher m = linkPattern.matcher(line);
                while (m.find()){
                    //System.out.println("orig page: " + title + " link: " + m.group(0));
                        links.put(title,m.group(1));
                }

            }

            if(findPageEnd(line)){

                //counter++;
                if(!links.isEmpty() && !redirects.containsKey(title)) {
                    JSONObject jsonL = new JSONObject(links);
                    fileWriter.write(jsonL.toJSONString());
                    fileWriter.write(System.lineSeparator());
                }
                //if(ns == 0)
                //titleToId.put(title,id);

                pageFound = false;
                titleFound = false;
                title = "";
                idFound = false;
                id = "";
                nsFound = false;
                ns = -1;
                links.clear();
            }
        }

        fileWriter.close();
    }



    private String findTitle(String line)
    {
        Matcher m = titlePattern.matcher(line);

        if(m.find())
            return m.group(1);
        else
            return null;
    }

    private int findNs(String line)
    {
        Matcher m = nsPattern.matcher(line);

        if(m.find())
            return parseInt(m.group(1));
        else
            return -1;
    }

    private String findID(String line)
    {
        Matcher m = idPattern.matcher(line);

        if(m.find())
            return m.group(1);
        else
            return null;
    }

    private boolean findPageStart(String line)
    {
        Matcher m = pageStartPattern.matcher(line);
        return m.find();
    }

    private boolean findPageEnd(String line)
    {
        Matcher m = pageEndPattern.matcher(line);
        return m.find();
    }

    private String detectRedirect(String line)
    {
        Matcher m = redirectPattern.matcher(line);
        if(m.find()){
            return  m.group(2);
        }
        else {
            return null;
        }
    }
}
