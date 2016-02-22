/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package performanceModel;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import commonmodules.ServerXen;
import commonmodules.pduPowerMeter;
import commonmodules.sshModule;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static performanceModel.webSearch.checkServerFreq;
import static performanceModel.webSearch.connectAllServers;

/**
 *
 * @author moislam
 */
public class webServ {
    private static final String user = "root";
    private static final String password = "srenserver";

    private static final String[] hosts = {"169.235.14.136",//server4-FE
                                            "169.235.14.139"}; //server7-BE
    private static final int[] noOfCore = {6,2};
    
    private static final String clinetUser = "testbed";
    private static final String clinetPassowrd = "srenserver";
    private static final String clinetIP = "169.235.14.103";
    private static int slotDuration = 120;
    
    
    public static void main(String[] args) throws JSchException, InterruptedException, IOException{
        
        
//        ServerXen[] servers = new ServerXen[hosts.length];
//        for (int i = 0; i < servers.length; i++) {
//            servers[i] = new ServerXen(hosts[i], user, password, noOfCore[i]);
//        }
//        
//        connectAllServers(servers);
//        checkServerFreq(servers);
        
        int[] ports = {21,18};
        pduPowerMeter powerMeter = new pduPowerMeter(ports, slotDuration);
        powerMeter.startLogging();
        
        sshModule sshClient = new sshModule(clinetIP, clinetUser, clinetPassowrd);
        Session session = sshClient.startSession();
        
        int[] noOfUsers={100,200,300,400,500,600,700,800,900,1000};
        for (int i=0; i<noOfUsers.length;i++){
            String command = "/faban/bin/fabancli submit OlioDriver test /home/testbed/inputConfig/run_"+noOfUsers[i]+".xml";
            String serverFeedback = sshClient.sendCommand(session, command);
            System.out.print(serverFeedback);
            Thread.sleep(5 * 1000); //allow staring of benchmark
            powerMeter.setLogId(i);
            startPowerMeters(powerMeter);
            Thread.sleep((slotDuration+20) * 1000); //allow benchmark to finish
        }
        
        
    }
    
    
    private static void startPowerMeters(pduPowerMeter powerMeter){
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
            meterRead.start();
    }
    
    
    
}
