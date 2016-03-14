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
public class hadoopMaster {

    private String masterIP;
    private String user;
    private String password;
    private int maxJobTime;
    
    sshModule sshHadoopMaster;
    Session session;

    public hadoopMaster(String IP, String user, String password) {
        this.masterIP = IP;
        this.user = user;
        this.password = password;
        this.sshHadoopMaster = new sshModule(masterIP, user, password);
        this.maxJobTime = 10;
    }
    
    public hadoopMaster(String IP, String user, String password, int maxTimeOut) {
        this.masterIP = IP;
        this.user = user;
        this.password = password;
        this.sshHadoopMaster = new sshModule(masterIP, user, password);
        this.maxJobTime = maxTimeOut;
    }

    public void startSession() throws JSchException {
        this.session = sshHadoopMaster.startSession();
    }
    
        public void disConnect() throws JSchException {
        this.session.disconnect();
    }
    
    public void deletFolder(String fileLoaction) throws JSchException {
        String command = "/usr/local/hadoop/bin/hadoop dfs -rm -r "+fileLoaction+" \n";
        String serverFeddbakc = sshHadoopMaster.sendCommand(session, command);
        System.out.print(serverFeddbakc);
    }
    
    public void startSort(String inputFile) throws JSchException{
        String command = "/usr/local/hadoop/bin/hadoop jar /usr/local/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.6.2.jar terasort /teraSort/input/"+inputFile+" /teraSort/output/"+inputFile+ " \n";
        String serverFeddbakc = sshHadoopMaster.sendCommandWcheck(session, command, this.maxJobTime);
        //System.out.print(serverFeddbakc);        
    }
    
    public void startCount(String inputFile) throws JSchException {
        String command = "hadoop jar /usr/local/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.6.4.jar wordcount -D mapreduce.job.reduces=15 "+ inputFile + " /wordCount/output \n";
        String serverFeddbakc = sshHadoopMaster.sendCommandWcheck(session, command, this.maxJobTime);
        System.out.print(serverFeddbakc);        
    }

}
