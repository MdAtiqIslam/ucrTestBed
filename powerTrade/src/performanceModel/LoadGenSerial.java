/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package performanceModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.io.FileWriter;
import java.util.Arrays;

/**
 *
 * @author scgclient
 */
public class LoadGenSerial implements Runnable{
    
    Thread thread;
    private int MaxNumberOfReq;
    private long StartTime;
    private long EndTime;
    private long ExpirationTime;
    private long[][] ResponseLog;
    private String[] searchIndex;
    private final Random randomGenerator = new Random();
    private int NoOfIndexTerms;
    private String threadNum;
    private int interReqSleepTime;
    
    private String nutchSiteIP;

    public LoadGenSerial(int threadNo, int interReqSleepTime, String nutchIP, long RunTime, int MaxNumberOfReq) {
        this.searchIndex = getSearchIndex();
        this.NoOfIndexTerms = searchIndex.length;
        this.interReqSleepTime = interReqSleepTime;
        this.MaxNumberOfReq = MaxNumberOfReq;
        ResponseLog = new long[MaxNumberOfReq][2];
        this.threadNum = String.valueOf(threadNo);
        this.nutchSiteIP=nutchIP;
        thread=new Thread(this,threadNum);
        this.StartTime = System.currentTimeMillis();
        this.ExpirationTime = this.StartTime + RunTime * 1000;
        thread.start();      
    }
    
    
    public LoadGenSerial(int threadNo, int interReqSleepTime, String nutchIP) {
        this.searchIndex = getSearchIndex();
        this.NoOfIndexTerms = searchIndex.length;
        this.interReqSleepTime = interReqSleepTime;
        this.MaxNumberOfReq = 1000;
        ResponseLog = new long[MaxNumberOfReq][2];
        this.threadNum = String.valueOf(threadNo);
        this.nutchSiteIP = nutchIP;
        thread = new Thread(this, threadNum);
        this.StartTime = System.currentTimeMillis();
        this.ExpirationTime = this.StartTime + 20 * 1000;
        thread.start();
    }
    
    
    
    
    
    private static String[] getSearchIndex(){
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
    
    private static double[] getResponseSummary(long[][] ResponseLog){
        int dataSize = ResponseLog.length;
        long[] ResponseLog1d = new long[dataSize];
        long sum = 0;
        for (int i = 0; i < dataSize; i++) {
            sum = sum + ResponseLog[i][1];
            ResponseLog1d[i] = ResponseLog[i][1];
        }
 
        Arrays.sort(ResponseLog1d);
        int index95 = (int) Math.ceil(0.95*dataSize);
        int index90 = (int) Math.ceil(0.90*dataSize);
        double[] summary = new double[4];

        summary[0] = sum/dataSize/(1000*1000); //convert nano to mili seconds
        summary[1] = ResponseLog1d[dataSize-1]/(1000*1000);
        summary[2] = ResponseLog1d[index95]/(1000*1000);
        summary[3] = ResponseLog1d[index90]/(1000*1000);
        
        return summary;
    }
    
    private void logToFile(long[][] ResponseLog, int noOfReq){
        try {
            //FileWriter writer = new FileWriter("C:\\Source Code\\atiq_codes\\LoadGenHttp\\results\\"+threadNum+"_"+System.currentTimeMillis()+".csv");
            String fileLocation = "C:\\local_files\\files\\output\\websearch\\nutch\\"+webSearch.logFolder+"_"+threadNum+".csv";
            FileWriter writer = new FileWriter(fileLocation);
//            writer.append("Send Time");
//            writer.append(',');
//            writer.append("Response");
//            writer.append('\n');
            
            for (int i = 0; i < noOfReq; i++) {
                writer.append(String.valueOf(ResponseLog[i][0]));
                writer.append(',');
                writer.append(String.valueOf(ResponseLog[i][1]));
                writer.append('\n');                
            }
            
            writer.flush();
            writer.close();
//        ReturnLog();
//        ReturnLogSummary(avg_response,index+1);
        } catch (IOException ex) {
            Logger.getLogger(LoadGenSerial.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        //String[] searchedItem = new String[MaxNumberOfReq];
        int index = 0;
        while( System.currentTimeMillis()<= ExpirationTime){
            try {
                //long start =  System.nanoTime();
                long start =  System.currentTimeMillis();
                String searchTerm = this.searchIndex[randomGenerator.nextInt(this.NoOfIndexTerms)];
                //String searchTerm = this.searchIndex[index];
                String searchURL = "http://"+nutchSiteIP+":8080/search.jsp?query="+searchTerm+"&hitsPerPage=10&lang=en";
                //URL searchTermUrl = new URL(searchTerm);
                URL nutch = new URL(searchURL);
                URLConnection yc = nutch.openConnection();
                try{
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                yc.getInputStream()));
                
                String inputLine;
                boolean stopSend = false;
                while ((inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if(System.currentTimeMillis()> ExpirationTime) {stopSend = true; in.close(); break;}
                }
               if (stopSend) break;
                
                in.close();
                }catch (java.net.SocketException | java.net.SocketTimeoutException excep){System.out.println("HTTP connection error \n");};
//                long end =  System.nanoTime();//
                 long end =  System.currentTimeMillis();
                 //if (end - start > 1000) continue;
                //System.out.println("Round trip response time = " + (end - start) + " millis");
                ResponseLog[index][0] = start;
//                ResponseLog[index][1] = (long)((end - start)/(1000*1000));
                ResponseLog[index][1] = end - start;
                //searchedItem[index] = searchTerm;
                long sleepTime = (long) ((-1)*interReqSleepTime*Math.log(1-randomGenerator.nextDouble()));
                if (sleepTime>1.3*interReqSleepTime) sleepTime = (long) (1.3*interReqSleepTime);
                Thread.sleep(sleepTime);
//                Thread.sleep(interReqSleepTime);
            } catch (MalformedURLException ex) {
                Logger.getLogger(LoadGenSerial.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException | InterruptedException  ex) {
                    Logger.getLogger(LoadGenSerial.class.getName()).log(Level.SEVERE, null, ex);
                }
            if (index>=MaxNumberOfReq-1) break;
            index++;
        }

        logToFile(ResponseLog, index);
        this.EndTime = System.currentTimeMillis();
//        double[] responseSummary = new double[4];
//        responseSummary = getResponseSummary(Arrays.copyOfRange(ResponseLog,0,index)); // returnf Average, Max, 95% and 90% response in ms

//        System.out.println("Run time: "+ (EndTime-StartTime)+ " ms, Avg. Response = " + 
//                responseSummary[0]+ " ms, 95% Response = " +responseSummary[2]+" ms, #Req "+ (index+1)+ " ,"+ thread);
                
    }
    
    private long[][] ReturnLog(){
        return ResponseLog;
    }
    
    private double[] ReturnLogSummary(double AveragResponse, int NoOfReq) {
        
        double[] summary = new double[2];
        summary[0] = AveragResponse;
        summary[1] = NoOfReq;
        return summary;
    }
    
}
