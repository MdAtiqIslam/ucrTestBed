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
import static hadoop.LoadGenHadoop.changeServerFreq;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author atiq
 */
public class expGraph {

    private static final String xenUser = "root";
    private static final String graphUser = "testbed";
    private static final String password = "srenserver";
    private static final String[] xenServerIPs = {"192.168.137.54", "192.168.137.55"};

    private static final String[] nodeIP = {"192.168.137.160", "192.168.137.217"};
    private static final int noOfCore = 6;
    private final static int slotDuration = 4 * 60;
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

        int[] allfreq = {1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2201};

        int[] ports = {21, 20};//{23,22};
        int[] activePorts = Arrays.copyOfRange(ports, 0, noOfServer);
        String powerLogLocation = "C:\\local_files\\files\\output\\graph\\power\\";
        pduPowerMeter powerMeter = new pduPowerMeter(activePorts, slotDuration, logPowerToFile, powerLogLocation);

        GraphAnalNode[] graphNodes = new GraphAnalNode[2];
        graphNodes[0] = new GraphAnalNode(nodeIP[0], graphUser, password, slotDuration);
        //graphNodes[1] = new GraphAnalNode(nodeIP[1], graphUser, password,slotDuration);
        graphNodes[0].startSession();
        //graphNodes[1].startSession();
        //int noOfUser=15000000;
        int noOfUser = 10000000;

        for (int rep = 0; rep < 2; rep++) {
            for (int i = 0; i < allfreq.length; i++) {
                //System.out.println("Power loging started \n");
                powerMeter.setLogId(i + rep * allfreq.length + 100);
                startPowerMeter(powerMeter);
                changeServerFreq(servers, allfreq[i]);
                Thread.sleep((0 + 10) * 1000);
                checkServerFreq(servers);
                Thread.sleep((0 + 5) * 1000);
                startGraph(graphNodes, noOfUser);

                Thread.sleep((slotDuration + 20) * 1000);
            }
        }

        changeServerFreq(servers, allfreq[0]);
        checkServerFreq(servers);
        System.out.println("!!!!!!!!!!Experiment runs started!!!!!!!!!!!!!!!!!!!");
        for (int i = 0; i < 15; i++) {
            powerMeter.setLogId(i);
            startPowerMeter(powerMeter);
            Thread.sleep((0 + 10) * 1000);
            startGraph(graphNodes, noOfUser);
            Thread.sleep((slotDuration + 20) * 1000);
        }

        graphNodes[0].disConnect();
        //graphNodes[1].disConnect();
        // master.disConnect();
        for (ServerXen server : servers) {
            server.ServerDisconnect();
        }

    }

    public static void startGraph(GraphAnalNode[] graphNodes, int noOfUser) {

        //for (int i = 0; i < graphNodes.length; i++) {
        GraphAnalNode currentNode = graphNodes[0];
        //int currentNodeID = i;
        int currentNodeID = 0;
        Thread nodeComm = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Graph started at Node" + currentNodeID + " at " + commonmodules.currentTime.getCurrentTime());
                    long garphStrat = System.currentTimeMillis();
                    currentNode.analSynth(noOfUser, noOfCore * 2);//To change body of generated methods, choose Tools | Templates.
                    long graphEnd = System.currentTimeMillis();
                    System.out.println("Node" + currentNodeID + " ends at " + commonmodules.currentTime.getCurrentTime());
                    System.out.println("Node" + currentNodeID + " total time" + (graphEnd - garphStrat) / 1000 + "seconds");
                } catch (JSchException ex) {
                    Logger.getLogger(expGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        nodeComm.start();
        //}
    }

    public static void startPowerMeter(pduPowerMeter powerMeter) {
        Thread meterRead = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    powerMeter.startLogging(); //To change body of generated methods, choose Tools | Templates.
                } catch (JSchException | InterruptedException | IOException ex) {
                    Logger.getLogger(expGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        //powerMeter.setLogId(ii + 1);
        meterRead.start();
    }

}
