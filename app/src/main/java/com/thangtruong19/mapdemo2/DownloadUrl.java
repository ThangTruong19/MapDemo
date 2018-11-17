package com.thangtruong19.mapdemo2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by User on 15/11/2018.
 */

public class DownloadUrl {
    public String readUrl(String myUrl) throws IOException{
        String data="";
        InputStream inputStream=null;
        HttpURLConnection urlConnection=null;

        try {
            URL url=new URL(myUrl);
            urlConnection=(HttpURLConnection)url.openConnection();
            urlConnection.connect();

            inputStream=urlConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer=new StringBuffer();

            String line="";
            line=bufferedReader.readLine();
            while (line!=null){
                stringBuffer.append(line);
                line=bufferedReader.readLine();
            }

            data=stringBuffer.toString();
            bufferedReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            inputStream.close();
            urlConnection.disconnect();
        }

        return data;
    }
}
