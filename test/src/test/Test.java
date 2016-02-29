/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import commonmodules.pduPowerMeter;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author moislam
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws JSchException, InterruptedException, IOException{
        
        int[] ports = {23,22,21,20,19,18,15,14,13};
        //int[] ports = {4,5,6,7};
        //pduPowerMeter powerMeter = new pduPowerMeter(ports, 10,1);
        
        //powerMeter.startLogging();
        
        
//        String user = "apc";
//        String password = "apc";
//        String host = "169.235.14.96";
//        // TODO code application logic here
//        sshModule sshTest = new sshModule(host, user, password);
//        
//        String command1 = "xenpm get-cpufreq-states 1";
//        String command2 = "xenpm get-cpufreq-states 2";
//        String command = "";
////        String[] commands = new String[] {"olReading 21,22,23 power \n", "olReading 11,12,13 power \n"};
//        
//        for (int i = 0; i <1000; i++) {
//            command+="date \n";
//            command += "olReading 21,22,23 power \n";
//        }
//        
//        String serverFeedback;
//        serverFeedback = null;
//        //sshTest.openChannel();
////        for (int i=0; i<=10;i++){
////        serverFeedback = sshTest.sendCommand(command);}
//        //serverFeedback = serverFeedback + "\n"+sshModule.sendCommand(session, command);
//        //System.out.println(serverFeedback);
//
//        
//        Session session = sshTest.startSession();
//        SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
//        String current_time_str = time_formatter.format(System.currentTimeMillis());
//        System.out.println("Starting time:"+current_time_str);
//        serverFeedback = sshTest.sendCommand(session,command);
////        serverFeedback = sshTest.sendCommand(session,command2);
//        sshTest.stopSession();

    }

}
