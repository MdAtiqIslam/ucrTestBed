/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scgclient
 */
public class LoadGenParallelReq implements Runnable{
    
    Thread thread;
    private String siteIP;
    private String searchString;
    private String threadNo;
    private responsePair response;
    
    public LoadGenParallelReq(int threadNo, String siteIP, String searchString, responsePair response){
        this.siteIP = siteIP;
        this.searchString = searchString;
        this.threadNo = String.valueOf(threadNo);
        this.response = response;        
        thread = new Thread(this, this.threadNo);
        thread.start();        
    }

    @Override
    public void run() {

        try {
            long start = System.currentTimeMillis();
            String searchURL = "http://" + siteIP + ":8080/search.jsp?query=" + searchString;
            URL nutch = new URL(searchURL);
            URLConnection yc = nutch.openConnection();
            BufferedReader in;
            
            in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                //System.out.println(inputLine);
            }
            in.close();
            long end = System.currentTimeMillis();
            response.setData(start,end-start);
            
            //int x=1;
            
            
        } catch (MalformedURLException ex) {
            //Logger.getLogger(LoadGenParallelReq.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Exception");
        } catch (IOException ex) {
            //Logger.getLogger(LoadGenParallelReq.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Exception");
        }
    }

}
