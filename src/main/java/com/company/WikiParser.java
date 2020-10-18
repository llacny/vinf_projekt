package com.company;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Map<String,String> getTitleTOIdMatching(File file) throws Exception {

        InputStream fileStream = new FileInputStream(file);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(fileStream);
        Reader decoder = new InputStreamReader(bzIn, UTF_8);
        BufferedReader bufferedReader = new BufferedReader(decoder);
        Map<String, String> pageTitleIds = new HashMap<>();

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

            while( (line = bufferedReader.readLine()) != null) {
                //for(int i = 0; i < 100000; i++){
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
                        //if(counter % 100 == 0)
                        //    System.out.println(counter);
                        titleFound = true;
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

                if(!nsFound){
                    ns = findNs(line);
                    if (ns == -1)
                        continue;
                    else{
                        nsFound = true;
                        continue;
                    }
                }

                if(findPageEnd(line)){
                    if(titleFound && idFound && nsFound)
                    {
                        //counter++;
                        if(ns == 0)
                            pageTitleIds.put(title,id);
                    }
                    else
                    {
                        System.out.println("Some data is missing");
                    }

                    pageFound = false;
                    titleFound = false;
                    idFound = false;
                    nsFound = false;
                }
            }

        return pageTitleIds;
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
}
