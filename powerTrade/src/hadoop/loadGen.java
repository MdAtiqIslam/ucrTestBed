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
public class loadGen {

    private static final String xenUser = "root";
    private static final String hadoopUser = "hduser";
    private static final String password = "srenserver";
    private static final String[] xenServerIPs = {"192.168.137.52", "192.168.137.53"};

    private static final String[] nodeIP = {"192.168.137.171", "192.168.137.172", "192.168.137.173", "192.168.137.174", "192.168.137.175", "192.168.137.176", "192.168.137.177", "192.168.137.178",
        "192.168.137.181", "192.168.137.182", "192.168.137.183", "192.168.137.184", "192.168.137.185", "192.168.137.186", "192.168.137.187", "192.168.137.188"};

    private static final int noOfCore = 16; //for servers 6 to 10
    private final static int slotDuration = 9*60;
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
        
        hadoopMaster master = new  hadoopMaster(nodeIP[0],hadoopUser,password);
        master.startSession(); 
        
        
        int[] ports = {23,22};
        int[] activePorts = Arrays.copyOfRange(ports,0,noOfServer);
        String powerLogLocation = "C:\\local_files\\files\\output\\hadoop\\power\\";
        pduPowerMeter powerMeter = new pduPowerMeter(activePorts,slotDuration,logPowerToFile,powerLogLocation);
        System.out.println("Power loging started \n");
        powerMeter.startLogging();
        
        //int[] allFreq = {1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600, 2601};
        int[] allFreq = {1300, 1400, 1600, 1700, 1900, 2000, 2200, 2300, 2500, 2600};
        
        String fileName="3G";
        for (int i=0; i<allFreq.length;i++){
            changeServerFreq(servers, allFreq[i]);
            checkServerFreq(servers);
            
            hadoopSort(master,fileName);
            powerMeterLog(powerMeter);
            Thread.sleep((slotDuration+60)*1000);
        }     
        
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
            try {
                server.setFreq(freq);
            } catch (JSchException ex) {
                Logger.getLogger(loadGen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void checkServerFreq(ServerXen[] servers) {

        for (ServerXen server : servers) {
            try {
                System.out.print(Arrays.toString(server.getFreqCurrent()) + "\n");
            } catch (JSchException ex) {
                Logger.getLogger(loadGen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    public static void powerMeterLog(pduPowerMeter powerMeter){
                    Thread meterRead = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        powerMeter.startLogging(); //To change body of generated methods, choose Tools | Templates.
                    } catch (JSchException | InterruptedException | IOException ex) {
                        Logger.getLogger(loadGen.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            meterRead.start();
    }

    
    public static void hadoopSort(hadoopMaster master, String fileName) {
        Thread sort = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    master.deletFolder("/teraSort/output/" + fileName);
                    System.out.print("Sorting started at " + commonmodules.currentTime.getCurrentTime());
                    long sortStrat = System.currentTimeMillis();
                    master.startSort(fileName);
                    long sortEnd = System.currentTimeMillis();
                    System.out.print(", Ends at " + commonmodules.currentTime.getCurrentTime());
                    System.out.print(",Total time" + (sortEnd - sortStrat) / 1000 + "seconds"); //To change body of generated methods, choose Tools | Templates.
                } catch (JSchException ex) {
                    Logger.getLogger(loadGen.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        sort.start();
    }

    
}
