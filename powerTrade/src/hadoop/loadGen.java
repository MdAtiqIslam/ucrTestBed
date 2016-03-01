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

    private static final String user = "hduser";
    private static final String password = "srenserver";
    private static final String[] xenServerIPs = {"192.168.137.52", "192.168.137.53"};

    private static final String[] nodeIP = {"192.168.137.171", "192.168.137.172", "192.168.137.173", "192.168.137.174", "192.168.137.175", "192.168.137.176", "192.168.137.177", "192.168.137.178",
        "192.168.137.181", "192.168.137.182", "192.168.137.183", "192.168.137.184", "192.168.137.185", "192.168.137.186", "192.168.137.187", "192.168.137.188"};

    private static final int noOfCore = 16; //for servers 6 to 10
    private final static int slotDuration = 60;
    public static long logFolder;
    public static boolean logPowerToFile = true;

    public static void main(String[] args) throws JSchException, InterruptedException, IOException {
        int noOfServer = 2;
        ServerXen[] servers = new ServerXen[noOfServer];
        for (int i = 0; i < servers.length; i++) {
            servers[i] = new ServerXen(xenServerIPs[i], user, password, noOfCore);
        }

        connectAllServers(servers);
        checkServerFreq(servers);
        
        
                int[] ports = {17,16,15};
        int[] activePorts = Arrays.copyOfRange(ports,0,noOfServer);
        String powerLogLocation = "C:\\local_files\\files\\output\\websearch\\power\\";
        pduPowerMeter powerMeter = new pduPowerMeter(activePorts,slotDuration+10,logPowerToFile,powerLogLocation);
        powerMeter.startLogging();
        
        int[] allFreq = {1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600, 2601};
        
        
        
        
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

}
