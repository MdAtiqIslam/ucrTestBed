/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author scgclient
 */
public class LoadGenParallelServer implements Runnable{
    
    Thread thread;
    private String siteIP;
    private int NoOfRequests;
    private int interArrivalTime;
    private int runTime;
    private String[] searchIndex;
    private int NoOfIndexTerms;
    private final Random randomGenerator = new Random();
    private long StartTime;
    private long ExpirationTime;
    private int serverNo;
    
    public LoadGenParallelServer(int serverNo, String siteIP, int runTime, int NoOfRequests){
        this.serverNo = serverNo;
        this.siteIP = siteIP;
        this.NoOfRequests = NoOfRequests;
        this.runTime = runTime;
        this.interArrivalTime = (int) Math.ceil(this.runTime*1000/NoOfRequests);
        this.searchIndex = getSearchIndex();
        this.NoOfIndexTerms = searchIndex.length;
        this.StartTime = System.currentTimeMillis();
        this.ExpirationTime = this.StartTime + this.runTime * 1000;
        thread = new Thread(this, String.valueOf((this.serverNo+1)*100000));
        thread.start();
    }
    
    @Override
    public void run() {
        int index = 0;
        responsePair[] response = new responsePair[NoOfRequests * 2];
        for (int i=0; i<response.length;i++){
            response[i] = new responsePair(0, 0);
        }
        List<LoadGenParallelReq> reqThreads = new ArrayList<>();

        while (System.currentTimeMillis() <= ExpirationTime) {
            String searchTerm = this.searchIndex[randomGenerator.nextInt(this.NoOfIndexTerms)];
            //response[index] = new responsePair(0, 0);
            reqThreads.add(new LoadGenParallelReq(index + serverNo * NoOfRequests, siteIP, searchTerm, response[index]));

            if (index >= NoOfRequests - 1) break;
            try {
                //Thread.sleep(randomGenerator.nextInt(interArrivalTime*2));
                long sleepTime = (long) ((-1) * interArrivalTime * Math.log(1 - randomGenerator.nextDouble()));
                if (sleepTime > 5 * interArrivalTime) {
                    sleepTime = (long) (5 * interArrivalTime);
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(LoadGenParallelServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            index++;
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LoadGenParallelServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        logToFile(response, index+1);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static String[] getSearchIndex() {
        String fileName = "C:\\local_files\\files\\data\\websearch\\search_terms.csv";
        int NoOfItem = 1609;
        String[] searchIndex = new String[NoOfItem];

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader
                    = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader
                    = new BufferedReader(fileReader);

            for (int i = 0; i < NoOfItem; i++) {
                searchIndex[i] = bufferedReader.readLine();
                //System.out.println(searchIndex[i]);
            }

            // Always close files.
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '"
                    + fileName + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                    + fileName + "'");
            // Or we could just do this: 
            // ex.printStackTrace();
        }

        return searchIndex;
    }

    private void logToFile(responsePair[] response, int noOfReq) {
        try {
            //FileWriter writer = new FileWriter("C:\\Source Code\\atiq_codes\\LoadGenHttp\\results\\"+threadNum+"_"+System.currentTimeMillis()+".csv");
            String fileLocation = "C:\\local_files\\files\\output\\websearch\\nutch\\"+webSearch.logFolder+"_"+serverNo+".csv";
            FileWriter writer = new FileWriter(fileLocation);
            
//            writer.append("Send Time");
//            writer.append(',');
//            writer.append("Response");
//            writer.append('\n');

            for (int i = 0; i < response.length; i++) {
                long[] reqResponse = response[i].getData();
                if (reqResponse[1]>0){
                writer.append(String.valueOf(reqResponse[0]));
                writer.append(',');
                writer.append(String.valueOf(reqResponse[1]));
                writer.append('\n');
                }
            }

            writer.flush();
            writer.close();
//        ReturnLog();
//        ReturnLogSummary(avg_response,index+1);
        } catch (IOException ex) {
            Logger.getLogger(LoadGenSerial.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
}
