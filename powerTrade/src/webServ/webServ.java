/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webServ;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import commonmodules.ServerXen;
import commonmodules.pduPowerMeter;
import commonmodules.sshModule;
import commonmodules.currentTime;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import webSearch.webSearch;

/**
 *
 * @author moislam
 */
public class webServ {

    private static final String user = "root";
    private static final String password = "srenserver";

//    private static final String[] hosts = {"169.235.14.136",//server4-FE
//                                            "169.235.14.139"}; //server7-BE
    private static final String[] hosts = {"192.168.137.55",//server5-FE
        "192.168.137.57"}; //server7-BE
    private static final int[] noOfCore = {6, 2};

    private static final String clinetUser = "testbed";
    private static final String clinetPassowrd = "srenserver";
    private static final String clinetIP = "192.168.137.103";
    private static int slotDuration = 120;

    public static void main(String[] args) throws JSchException, InterruptedException, IOException {

        ServerXen[] servers = new ServerXen[hosts.length];
        for (int i = 0; i < servers.length; i++) {
            servers[i] = new ServerXen(hosts[i], user, password, noOfCore[i]);
        }

        connectAllServers(servers);
        checkServerFreq(servers);

        int[] freqFE = {1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2201};
        int[] freqBE = {1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600};

        int[] ports = {20, 18};
        String powerLogLocation = "C:\\local_files\\files\\output\\webserv\\power\\";
        pduPowerMeter powerMeter = new pduPowerMeter(ports, slotDuration * 4, true, powerLogLocation);
        //powerMeter.startLogging();

        sshModule sshClient = new sshModule(clinetIP, clinetUser, clinetPassowrd);
        Session session = sshClient.startSession();

        int[] noOfUsers = {200, 400, 600, 800, 1000, 1200};
        //int[] noOfUsers = {1200,1400,1600,1800,2000};

        for (int f = 5; f >=0; f--) {
            int[] currentFreq = {freqFE[2 * f + 1], freqBE[2 * f + 4]};
            changeServerFreq(servers, currentFreq);
            checkServerFreq(servers);

            for (int i = 0; i < noOfUsers.length; i++) {
                for (int rep = 0; rep < 3; rep++) {
                    powerMeter.setLogId(i + f * noOfUsers.length);
                    startPowerMeters(powerMeter);
                    String command = "faban/bin/fabancli submit OlioDriver test /home/testbed/inputConfig/run_" + noOfUsers[i] + ".xml \n";
                    String serverFeedback = sshClient.sendCommand(session, command);
                    System.out.print(serverFeedback);
                    Thread.sleep((slotDuration * 4 + 60) * 1000); //allow benchmark to finish
                }
            }
        }

        for (ServerXen server : servers) {
            server.ServerDisconnect();
        }

    }

    private static void startPowerMeters(pduPowerMeter powerMeter) {
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
        meterRead.start();
    }

    public static void changeServerFreq(ServerXen[] servers, int[] freq) throws JSchException {
        for (int i = 0; i < servers.length; i++) {
            servers[i].setFreq(freq[i]);
        }

    }

    public static void connectAllServers(ServerXen[] servers) {
        //connect all servers to control, must disconnet later
        for (ServerXen server : servers) {
            server.ServerConnect();
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
