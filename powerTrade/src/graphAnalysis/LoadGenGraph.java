/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphAnalysis;

import com.jcraft.jsch.JSchException;
import commonmodules.ServerXen;
import commonmodules.pduPowerMeter;
import static hadoop.LoadGenHadoop.checkServerFreq;
import static hadoop.LoadGenHadoop.connectAllServers;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author atiq
 */
public class LoadGenGraph {

    private static final String xenUser = "root";
    private static final String graphUser = "testbed";
    private static final String password = "srenserver";
    private static final String[] xenServerIPs = {"192.168.137.160", "192.168.137.217"};

    private static final String[] nodeIP = {"192.168.137.205", "192.168.137.254"};
    private static final int noOfCore = 8;
    private final static int slotDuration = 15 * 60;
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
        
            int[] ports = {23, 22};//{23,22};
            int[] activePorts = Arrays.copyOfRange(ports, 0, noOfServer);
            String powerLogLocation = "C:\\local_files\\files\\output\\hadoop\\power\\";
            pduPowerMeter powerMeter = new pduPowerMeter(activePorts, slotDuration, logPowerToFile, powerLogLocation);
            System.out.println("Power loging started \n");
            powerMeter.startLogging();
            
            GraphAnalNode node1 = new  GraphAnalNode(nodeIP[0],graphUser,password);
            GraphAnalNode node2 = new  GraphAnalNode(nodeIP[0],graphUser,password);

        
        
           // master.disConnect();
            for (ServerXen server : servers) {
                server.ServerDisconnect();
            }
        
        }
    
    
}
