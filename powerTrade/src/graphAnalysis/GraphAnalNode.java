/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphAnalysis;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import commonmodules.sshModule;

/**
 *
 * @author atiq
 */
public class GraphAnalNode {
    
    private String nodeIP;
    private String user;
    private String password;
    private int maxJobTime;

    sshModule sshGraphNode;
    Session session;
    
        public GraphAnalNode(String IP, String user, String password) {
        this.nodeIP = IP;
        this.user = user;
        this.password = password;
        this.sshGraphNode = new sshModule(nodeIP, user, password);
        this.maxJobTime = 10;
    }
    
    public GraphAnalNode(String IP, String user, String password, int maxTimeOut) {
        this.nodeIP = IP;
        this.user = user;
        this.password = password;
        this.sshGraphNode = new sshModule(nodeIP, user, password);
        this.maxJobTime = maxTimeOut;
    }

    public void startSession() throws JSchException {
        this.session = sshGraphNode.startSession();
    }
    
        public void disConnect() throws JSchException {
        this.session.disconnect();
    }
        
    public void analTwiter(int noCPU) throws JSchException {
        String command = "PowerGraph/release/toolkits/graph_analytics/tunkrank --graph=/home/testbed/Twitter-dataset/data/twitter_small_data_graplab.in --format=tsv --ncpus="+noCPU+" --engine=asynchronous";
        String serverFeddbakc = sshGraphNode.sendCommandWcheck(session, command, this.maxJobTime);
        //System.out.print(serverFeddbakc);        
    }
    
    public void analSynth(int vertices, int noCPU) throws JSchException {
        String command = "PowerGraph/release/toolkits/graph_analytics/tunkrank --powerlaw="+vertices+" --ncpus="+noCPU+" --engine=asynchronous";
        String serverFeddbakc = sshGraphNode.sendCommandWcheck(session, command, this.maxJobTime);
        //System.out.print(serverFeddbakc);        
    }

    
    
}
