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
    
    sshModule sshHadoopMaster;
    Session session;

    public hadoopMaster(String IP, String user, String password) {
        this.masterIP = IP;
        this.user = user;
        this.password = password;
        this.sshHadoopMaster = new sshModule(masterIP, user, password);
    }
    
    public void startSession() throws JSchException {
        this.session = sshHadoopMaster.startSession();
    }
    
    public void deletFolder(String fileLoaction) throws JSchException {
        String command = "/usr/local/hadoop/bin/hadoop dfs -rm -r "+fileLoaction+" \n";
        System.out.print(sshHadoopMaster.sendCommand(session, command));
    }
    
    public void startSort(String inputFile) throws JSchException{
        String command = "/usr/local/hadoop/bin/hadoop jar /usr/local/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.6.2.jar terasort /teraSort/input/"+inputFile+" /teraSort/output/"+inputFile+ " \n";
        System.out.print(sshHadoopMaster.sendCommandWcheck(session, command));
        
    }

}
