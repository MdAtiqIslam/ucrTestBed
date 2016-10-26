/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hadoop;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import commonmodules.sshModule;

/**
 *
 * @author atiq
 */
public class cleanFS {

    private static final String user = "hduser";
    private static final String password = "";
//
    private static final String[] nodeIP = {"192.168.137.171", "192.168.137.172", "192.168.137.173", "192.168.137.174", "192.168.137.175", "192.168.137.176", "192.168.137.177", "192.168.137.178",
        "192.168.137.181", "192.168.137.182", "192.168.137.183", "192.168.137.184", "192.168.137.185", "192.168.137.186", "192.168.137.187", "192.168.137.188"};
//    private static final String[] nodeIP = {"192.168.137.171", "192.168.137.181", "192.168.137.182", "192.168.137.183", "192.168.137.184"};//
  //  private static final String[] nodeIP = {"192.168.137.171", "192.168.137.172", "192.168.137.173", "192.168.137.174","192.168.137.175","192.168.137.176","192.168.137.177","192.168.137.178"};

    
//        private static final String[] nodeIP = {"192.168.137.175", "192.168.137.176", "192.168.137.177", "192.168.137.178",
//        "192.168.137.181", "192.168.137.182", "192.168.137.183", "192.168.137.184", "192.168.137.185", "192.168.137.186", "192.168.137.187", "192.168.137.188"};

    
    
    public static void main(String[] args) throws JSchException, InterruptedException {
        // TODO code application logic here
        //cleanmaster
        sendCommand(nodeIP[0], user, password, true);
        for (int i = 1; i < nodeIP.length; i++) {
            sendCommand(nodeIP[i], user, password, false);
        }
        
//        for (int i = 0; i < nodeIP.length; i++) {
//            sendCommandClearHadoop(nodeIP[i], user, password);
//        }
//        
        
    }

    private static void sendCommand(String host, String user, String password, boolean isMaster) throws JSchException, InterruptedException {
        String command;
        if (isMaster) {
            command = "sudo rm -rf /usr/local/hadoop_tmp/hdfs/ \n"
                    + "sudo mkdir -p /usr/local/hadoop_tmp/hdfs/namenode \n"
                    + "sudo chown hduser:hadoop -R /usr/local/hadoop_tmp/ \n";
        } else {
            command = "sudo rm -rf /usr/local/hadoop_tmp/hdfs/ \n"
                    + "sudo mkdir -p /usr/local/hadoop_tmp/hdfs/datanode \n"
                    + "sudo chown hduser:hadoop -R /usr/local/hadoop_tmp/ \n";
        }
        sshModule sshNode = new sshModule(host, user, password);
        Session session = sshNode.startSession();
        System.out.println("Cleaning node"+host+"\n");
        System.out.print(sshNode.sendCommand(session, command));
//        Thread.sleep(1000 * 2);
        sshNode.stopSession();
    }
    
        private static void sendCommandClearHadoop(String host, String user, String password) throws JSchException, InterruptedException {
        String command;
            command = "sudo rm -rf /usr/local/hadoop \n"
                    + "sudo mkdir -p /usr/local/hadoop \n"
                    + "sudo chown hduser:hadoop -R /usr/local/hadoop \n";

        sshModule sshNode = new sshModule(host, user, password);
        Session session = sshNode.startSession();
        System.out.println("Cleaning node"+host+"\n");
        System.out.print(sshNode.sendCommand(session, command));
//        Thread.sleep(1000 * 2);
        sshNode.stopSession();
    }

}
