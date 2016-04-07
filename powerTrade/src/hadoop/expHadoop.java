/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hadoop;

import com.jcraft.jsch.JSchException;
import commonmodules.ServerXen;
import commonmodules.pduPowerMeter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author atiq
 */
public class expHadoop {

    private static final String xenUser = "root";
    private static final String hadoopUser = "hduser";
    private static final String password = "srenserver";
    private static final String[] xenServerIPs = {"192.168.137.52", "192.168.137.53"};

    private static final String[] nodeIP = {"192.168.137.171", "192.168.137.172", "192.168.137.173", "192.168.137.174", "192.168.137.175", "192.168.137.176", "192.168.137.177", "192.168.137.178",
        "192.168.137.181", "192.168.137.182", "192.168.137.183", "192.168.137.184", "192.168.137.185", "192.168.137.186", "192.168.137.187", "192.168.137.188"};

    private static final int noOfCore = 16; //for servers 6 to 10
    private final static int slotDuration = 10*60;
    public static long logFolder;
    public static boolean logPowerToFile = true;


    public static void main(String[] args) throws JSchException, InterruptedException, IOException {
        int noOfServer = 2;
        ServerXen[] servers = new ServerXen[noOfServer];
        for (int i = 0; i < servers.length; i++) {
            servers[i] = new ServerXen(xenServerIPs[i], xenUser, password, noOfCore);
        }

        connectAllServers(servers);
        checkServerFreq(servers);
        
        int maxTimeOut = slotDuration;
        hadoopMaster master = new  hadoopMaster(nodeIP[0],hadoopUser,password,maxTimeOut);
        master.startSession(); 
        
        
        int[] ports = {23,22};//{23,22};
        int[] activePorts = Arrays.copyOfRange(ports,0,noOfServer);
        String powerLogLocation = "C:\\local_files\\files\\output\\hadoop\\power\\";
        pduPowerMeter powerMeter = new pduPowerMeter(activePorts,slotDuration,logPowerToFile,powerLogLocation);
//        System.out.println("Power loging started \n");
        
        
        int[] allFreq = {1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600, 2601};
        //int[] allFreq = {1300, 1400, 1600, 1700, 1900, 2000, 2200, 2300, 2500, 2600};
//        changeServerFreq(servers, allFreq[0]);
//        checkServerFreq(servers);
//        Thread.sleep(60*1000);

        //int[] expFreq={2200,1900,1600,2300,1800,1700,1300,1700,2200,2100};
        //int[] expFreq={2000,1800,1400,2000,1800,1600,1400,1800,1900,1900};
        
        
        //String fileName="/wordCount/input";
        String fileName="5G";
        
        for (int i = 0; i < allFreq.length; i++) {

            int timeIndex = (int) System.currentTimeMillis();
            powerMeter.setLogId(timeIndex);
            //System.out.println("Power loging started \n");
            powerMeterLog(powerMeter);
            //changeServerFreq(servers, expFreq[8]);
            changeServerFreq(servers, allFreq[i]);
            Thread.sleep((60) * 1000);
            checkServerFreq(servers);
            //hadoopStart(master, fileName);
            hadoopStartSort(master, fileName);

            Thread.sleep((slotDuration) * 1000);
        }
//        Thread.sleep((5)*1000);
//        for (int i=9;i<expFreq.length;i++){
//            Thread.sleep((120-5)*1000);
//            changeServerFreq(servers, expFreq[i]);
//            Thread.sleep((5)*1000);
//            checkServerFreq(servers);
//        }


        
        //Thread.sleep((15*60)*1000);
        
//        
//        for (int i = 0; i < allFreq.length; i++) {
//            powerMeter.setLogId(i);
//            changeServerFreq(servers, allFreq[i]);
//            checkServerFreq(servers);
//            powerMeterLog(powerMeter);
//            Thread.sleep((60)*1000);
//            hadoopStart(master, fileName);
//            //serverFreqChange(allFreq,servers);
//            Thread.sleep((slotDuration)*1000);
//        }

//        

        master.disConnect();
        for (ServerXen server : servers) {
            server.ServerDisconnect();
        } 

    }
    
    public static void startHadoopJob(){
        
    }

    public static void connectAllServers(ServerXen[] servers) {
        //connect all servers to control, must disconnet later
        for (ServerXen server : servers) {
            server.ServerConnect();
        }
    }
    
    public static void changeServerFreq(ServerXen[] servers, int freq) {

        for (ServerXen server : servers) {
            Thread chnageFreqParallel = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        server.setFreq(freq);
                    } catch (JSchException ex) {
                        Logger.getLogger(expHadoop.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            chnageFreqParallel.start();
        }
    }

    public static void checkServerFreq(ServerXen[] servers) {

        for (ServerXen server : servers) {
            Thread getFreqParallel = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                System.out.print(Arrays.toString(server.getFreqCurrent()) + "\n");
            } catch (JSchException ex) {
                        Logger.getLogger(expHadoop.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            getFreqParallel.start();
        }

    }
    
    public static void powerMeterLog(pduPowerMeter powerMeter){
                Thread meterRead = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        powerMeter.startLogging(); //To change body of generated methods, choose Tools | Templates.
                    } catch (JSchException | InterruptedException | IOException ex) {
                        Logger.getLogger(LoadGenHadoop.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            meterRead.start();
    }

    
    public static void hadoopStart(hadoopMaster master, String fileName) {
        Thread jobHadoop = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //master.deletFolder("/teraSort/output/" + fileName);
                    master.deletFolder("/wordCount/output");
                    System.out.print("Count started at " + commonmodules.currentTime.getCurrentTime());
                    long countStrat = System.currentTimeMillis();
                    master.startCount(fileName);
                    long countEnd = System.currentTimeMillis();
                    System.out.print(", Ends at " + commonmodules.currentTime.getCurrentTime());
                    System.out.print(",Total time" + (countEnd - countStrat) / 1000 + "seconds");
                    //To change body of generated methods, choose Tools | Templates.
                } catch (JSchException ex) {
                    Logger.getLogger(LoadGenHadoop.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jobHadoop.start();
    }
    
    
        public static void hadoopStartSort(hadoopMaster master, String fileName) {
        Thread jobHadoop = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    master.deletFolder("/teraSort/output" + fileName);
                    //master.deletFolder("/wordCount/output");
                    System.out.print("Sort started at " + commonmodules.currentTime.getCurrentTime());
                    long countStrat = System.currentTimeMillis();
                    master.startSort(fileName);
                    long countEnd = System.currentTimeMillis();
                    System.out.print(", Ends at " + commonmodules.currentTime.getCurrentTime());
                    System.out.print(",Total time" + (countEnd - countStrat) / 1000 + "seconds");
                    //To change body of generated methods, choose Tools | Templates.
                } catch (JSchException ex) {
                    Logger.getLogger(LoadGenHadoop.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jobHadoop.start();
    }
    
    
    public static void serverFreqChange(int[] allFreq, ServerXen[] servers) {
        Thread freqChange = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < allFreq.length; i += 2) {
                        Thread.sleep(100 * 1000);
                        changeServerFreq(servers, allFreq[i]);
                        checkServerFreq(servers);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(LoadGenHadoop.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        freqChange.start();
    }

    
}
