/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package performanceModel;

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
public class webSearch {

    private static final String user = "root";
    private static final String password = "SCGServer@15";

    private static final String[] hosts = {"169.235.14.144",//server12
                                            "169.235.14.146"}; //server14
// TODO: Hostname of the remote machine (eg: inst.eecs.berkeley.edu)

    private static final String[] nutchIP = {"192.168.137.7",
                                                "192.168.137.219"};
    private static final int noOfCore = 6; //for servers 6 to 10
    private static int slotDuration = 10;

    public static void main(String[] args) throws JSchException, InterruptedException {
        int noOfServer = 2;
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

        int[] ports = {17, 16};
        pduPowerMeter powerMeter = new pduPowerMeter(ports, slotDuration);

        //int[] allFreq = {1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600};
        int[] allFreq = {1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2001};
        for (int expNo = 0; expNo < allFreq.length; expNo++) {
            changeServerFreq(servers, allFreq[expNo]);
            checkServerFreq(servers);
            loadGenSerial(noOfServer, powerMeter);
        }

        //disconnect servers
        for (ServerXen server : servers) {
            server.ServerDisconnect();
        }

    }

    public static void loadGenSerial(int NoOfServers, pduPowerMeter powerMeter) throws InterruptedException {

        //int[] interArrivalTime = {0,100,200,300,400,500,600,700,800,900,1000,1100,1200,1300,1400,1500}; 
        int interArrivalTime = 200;
        int[] NoOfThreadsArray = {5, 10, 15, 20, 25, 35, 40, 45, 50, 55, 60, 65, 70};//{5,10,15,20,25,35,40,45,50,55,60,65,70};//{10,20,30,40,50,60,70,80,90,100};
        for (int nt = 0; nt < NoOfThreadsArray.length; nt++) {
            int NoOfThreads = NoOfThreadsArray[nt];

            for (int j = 0; j < NoOfServers; j++) {
                List<LoadGenSerial> test = new ArrayList<>();
                for (int i = 0; i < NoOfThreads; i++) {
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
            String csvFile = "C:\\local_files\\files\\output\\websearch\\nutch\\" + i + ".csv";
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
                e.printStackTrace();
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
        int index95 = (int) Math.ceil(0.95 * totalRequest);
        int index90 = (int) Math.ceil(0.90 * totalRequest);
        double[] responseSummary = new double[4];

        responseSummary[0] = sum / totalRequest; //data in milisecond
        responseSummary[1] = allResponse[totalRequest - 1]; //maximum response time
        responseSummary[2] = allResponse[index95]; //95% response time
        responseSummary[3] = allResponse[index90]; //90% response time

        System.out.println(", Log: " + LogNumber + ", Avg. Response = " + responseSummary[0] + " ms, 95% Response = "
                + responseSummary[2] + " ms, #Req " + totalRequest + " ,");

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
