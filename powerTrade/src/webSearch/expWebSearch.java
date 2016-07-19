/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSearch;

import com.jcraft.jsch.JSchException;
import commonmodules.ServerXen;
import commonmodules.pduPowerMeter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author moislam
 */
public class expWebSearch {

    private static final String user = "root";
    private static final String password = "srenserver";

    private static final String[] hosts = {"192.168.137.61",//server11
                                            "192.168.137.62",//server12
                                            "192.168.137.63"};//server13
// TODO: Hostname of the remote machine (eg: inst.eecs.berkeley.edu)

    private static final String[] nutchIP = {"192.168.137.201","192.168.137.202","192.168.137.203"};
    private static final int noOfCore = 6; //for servers 6 to 10
    private final static int slotDuration = 120;
    public static long logFolder;
    public static boolean logPowerToFile=true;
    

    public static void main(String[] args) throws JSchException, InterruptedException, IOException {
        
        int noOfServer = 2 ;
        ServerXen[] servers = new ServerXen[noOfServer];
        for (int i = 0; i < servers.length; i++) {
            servers[i] = new ServerXen(hosts[i], user, password, noOfCore);
        }

        connectAllServers(servers);
        checkServerFreq(servers);
//        for (ServerXen server : servers) {
//            ArrayList<Long> speeds = server.getFreqAavailabe();
//            for (int i = 0; i < speeds.size(); i++) {
//                System.out.println(speeds.get(i));
//            }
//        }


        int[] ports = {14,12,11};//{14,12,11}
        int[] activePorts = Arrays.copyOfRange(ports,0,noOfServer);
        String powerLogLocation = "C:\\local_files\\files\\output\\websearch\\power\\";
        pduPowerMeter powerMeter = new pduPowerMeter(activePorts,slotDuration*4,logPowerToFile,powerLogLocation);
        int timeIndex = (int) System.currentTimeMillis(); 
        
        Thread meterRead = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    powerMeter.startLogging(); //To change body of generated methods, choose Tools | Templates.
                } catch (JSchException | InterruptedException | IOException ex) {
                    Logger.getLogger(webSearch.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        powerMeter.setLogId(timeIndex);
        meterRead.start();


        //int[] allFreq = {1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600};
        //int[] allFreq = {1200, 1200, 1800, 1200, 1300, 1200, 2000, 1300, 1200, 1200};
        int[] allFreq = {1200, 2001, 2001};
        for (int i=0; i<allFreq.length;i++){
        changeServerFreq(servers, allFreq[i]);
        checkServerFreq(servers);
        //loadGenSerial(noOfServer, powerMeter,expNo+repeat*allFreq.length);
        loadGenParallel(noOfServer, powerMeter, i);
        }

        Thread.sleep(2*60 * 1000);
        //loadGenParallel(noOfServer,powerMeter);

        //disconnect servers
        for (ServerXen server : servers) {
            server.ServerDisconnect();
        }

    }
    
    
    
        public static void loadGenParallel(int NoOfServers, pduPowerMeter powerMeter,int runID) throws InterruptedException{
            
        //int[] NoOfReq = {100,500,1000,1500,2000,2500,3000,3500,4000};//, 3000, 3500, 4000};//, 3300, 3400, 3500};
        //int[] reqPerSec = {20, 40, 60, 80, 100, 120, 140, 160, 180, 200};
        //int[] reqPerSecAll = {80,40,320,160,280,120,320,200,120,80};
        //int[] reqPerSecAll = {300,300,300};
        //int[] reqPerSec = new int[1];
        //reqPerSec[0]=reqPerSecAll[runID];
        //int[] reqPerSec = {400};
        int[] reqPerSec = {300};
        
        int[] noOfReq = new int[reqPerSec.length];
        for (int i=0;i<reqPerSec.length;i++){
            noOfReq[i]=reqPerSec[i]/NoOfServers*slotDuration;
        }
        for (int ii = 0; ii < noOfReq.length; ii++) {
            
//            Thread meterRead = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        powerMeter.startLogging(); //To change body of generated methods, choose Tools | Templates.
//                    } catch (JSchException | InterruptedException | IOException ex) {
//                        Logger.getLogger(webSearch.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            });
//            powerMeter.setLogId(runID);
//            meterRead.start();
            Thread.sleep(10 * 1000);
            
            logFolder=System.currentTimeMillis();
            List<LoadGenParallelServer> test = new ArrayList<>();
            
            for (int j = 0; j < NoOfServers; j++) {     //NoOfServers       
                test.add(new LoadGenParallelServer(j, nutchIP[j], slotDuration, noOfReq[ii]));
            }
                Thread.sleep((slotDuration+5) * 1000);

            //summary of response from the server
            //responseSummarizer(0, NoOfServers, noOfReq[ii]);
        }
    }
    
    
    
    
    
    public static void  loadGenSerial(int NoOfServers, pduPowerMeter powerMeter, int runID) throws InterruptedException, JSchException, IOException {

        //int[] interArrivalTime = {0,100,200,300,400,500,600,700,800,900,1000,1100,1200,1300,1400,1500}; 
        int interArrivalTime = 200;
        int[] NoOfThreadsArray = {5};//,40,50,60,70};//{5, 10, 15, 20, 25, 35, 40, 45, 50, 55, 60, 65, 70};//{5,10,15,20,25,35,40,45,50,55,60,65,70};//{10,20,30,40,50,60,70,80,90,100};
        for (int nt = 0; nt < NoOfThreadsArray.length; nt++) {
            logFolder=System.currentTimeMillis();
           
            int NoOfThreads = NoOfThreadsArray[nt];
            
            Thread meterRead = new Thread (new Runnable(){
                @Override
                public void run() {
                    try {
                        powerMeter.startLogging(); //To change body of generated methods, choose Tools | Templates.
                    } catch (JSchException | InterruptedException | IOException ex) {
                        Logger.getLogger(webSearch.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            powerMeter.setLogId(nt+NoOfThreadsArray.length*runID);
            meterRead.start();
            
            
            List<LoadGenSerial> test = new ArrayList<>();
            for (int i = 0; i < NoOfThreads; i++) {
                for (int j = 0; j < NoOfServers; j++) {
                    test.add(new LoadGenSerial(i + j * NoOfThreads, interArrivalTime, nutchIP[j], slotDuration, 1000));
                }
            }
            Thread.sleep((slotDuration+20) * 1000);

            //log power meter radings into files
            //summary of response from the server
            responseSummarizer(0, NoOfThreads * NoOfServers, NoOfThreads);
        }
    }

    public static void responseSummarizer(int startNumber, int NoOfFiles, int LogNumber) {

        List<Double> allData = new ArrayList<>();
        for (int i = startNumber; i < startNumber + NoOfFiles; i++) {
            String csvFile = "C:\\local_files\\files\\output\\websearch\\nutch\\" +webSearch.logFolder+"_"+ i + ".csv";
            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";
            try {
                br = new BufferedReader(new FileReader(csvFile));
                while ((line = br.readLine()) != null) {

                    // use comma as separator
                    String[] data = line.split(cvsSplitBy);
                    allData.add(Double.parseDouble(data[1]));
                }
            } catch (IOException e) {
                //e.printStackTrace();
              System.out.println("File "+csvFile+" not found");
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        int totalRequest = allData.size();
        double[] allResponse = new double[totalRequest];
        for (int i = 0; i < totalRequest; i++) {
            allResponse[i] = allData.get(i);
        }

        double sum = 0;
        for (int i = 0; i < totalRequest; i++) {
            sum = sum + allResponse[i];
        }

        Arrays.sort(allResponse);
        int index99 = (int) Math.ceil(0.99 * totalRequest);
        int index95 = (int) Math.ceil(0.95 * totalRequest);
        double[] responseSummary = new double[3];

        responseSummary[0] = sum / totalRequest; //data in milisecond
        //responseSummary[1] = allResponse[totalRequest - 1]; //maximum response time
        responseSummary[1] = allResponse[index99]; //99% response time
        responseSummary[2] = allResponse[index95]; //95% response time

        System.out.println(", Log: " + LogNumber + ", Avg. Response = " + responseSummary[0] 
                + " ms, 95% Response = "+ responseSummary[2] 
                + " ms, 99% Response = "+ responseSummary[1] + " ms, #Req " + totalRequest + " ,");

    }

    public static void connectAllServers(ServerXen[] servers) {
        //connect all servers to control, must disconnet later
        for (ServerXen server : servers) {
            server.ServerConnect();
        }
    }

    public static void changeServerFreq(ServerXen[] servers, int freq) {
        for (ServerXen server : servers) {
            try {
                server.setFreq(freq);
            } catch (JSchException ex) {
                Logger.getLogger(webSearch.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void checkServerFreq(ServerXen[] servers) {

        for (ServerXen server : servers) {
            try {
                System.out.print(Arrays.toString(server.getFreqCurrent()) + "\n");
            } catch (JSchException ex) {
                Logger.getLogger(webSearch.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}

