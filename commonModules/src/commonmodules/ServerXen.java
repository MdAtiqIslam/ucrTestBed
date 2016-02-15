/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commonmodules;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;

/**
 *
 * @author Mohammad
 */
public class ServerXen {

    private String host;
    private String user;
    private String password;
    private Session session;
    private sshModule sshConnection;
    
    private int NoOfCore;
    private String[] cpu_uuid;
    private HashMap VMNameMap = new HashMap(); //keeps VM name to uuid map
    private HashMap VMNumberMap = new HashMap(); //keeps VM number to VM name map
    private int NoOfVM;
    private int Max_VM=20;



    public ServerXen(String host) {
        this.host = host;
        this.user = "root";
        this.password = "SCGServer@15";
        this.NoOfCore = 6;
        cpu_uuid = new String[NoOfCore];
    }

    public ServerXen(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.NoOfCore = 6;
        cpu_uuid = new String[NoOfCore];
    }

    public ServerXen(String host, String user, String password, int NoOfCore) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.NoOfCore = NoOfCore;
        cpu_uuid = new String[NoOfCore];
    }    
    
    public void ServerConnect(){
        this.sshConnection = new sshModule(host, user, password);
        try {
        session = this.sshConnection.startSession();
        } catch (JSchException ex) {
            Logger.getLogger(ServerXen.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //return session;
        
        //return session;
    }
    
    
        public void ServerDisconnect(){
            this.session.disconnect();
    }
        
    public void getCPUid() throws JSchException { 

        String command = "xe host-cpu-list" + " \n";
        String serverFeedback = null;
        serverFeedback = sshConnection.sendCommand(this.session, command);
        
        //String[] lines = serverFeedback.split(System.getProperty("line.separator"));
        
        String[] uuid_temp = new String[NoOfCore];
        Pattern pattern = Pattern.compile("uuid.*?: (.*?)\r\n");
        Matcher matcher = pattern.matcher(serverFeedback);
        int index = 0;
        while (matcher.find()) {
            uuid_temp[index] = matcher.group(1);
            index++;
        }
        
        int[] cpu_number = new int[NoOfCore];
        pattern = Pattern.compile("number.*?: (.*?)\r\n");
        matcher = pattern.matcher(serverFeedback);
        index = 0;
        while (matcher.find()) {
            cpu_number[index] = Integer.parseInt(matcher.group(1));
            index++;
        }
        
        
//        System.out.println(Arrays.toString(uuid_temp));
//        System.out.println(Arrays.toString(cpu_number));
        
        for (int i = 0; i < NoOfCore; i++) {
            
            this.cpu_uuid[cpu_number[i]] = uuid_temp[i];
        }
        
//        System.out.println(Arrays.toString(cpu_uuid));

    }
    
    public void getVMid() throws JSchException{
        
        String command = "xe vm-list power-state=running" + " \n";
        String serverFeedback = null;
        serverFeedback = sshConnection.sendCommand(this.session, command);
        
        //String[] lines = serverFeedback.split(System.getProperty("line.separator"));
        
        String[] uuid_temp = new String[this.Max_VM];
        Pattern pattern = Pattern.compile("uuid.*?: (.*?)\r\n");
        Matcher matcher = pattern.matcher(serverFeedback);
        int index = 0;
        while (matcher.find()) {
            uuid_temp[index] = matcher.group(1);
            index++;
        }
        //VMidMap.put();
        
        String[] VM_Name = new String[this.Max_VM];
        pattern = Pattern.compile("name-label.*?: (.*?)\r\n");
        matcher = pattern.matcher(serverFeedback);
        index = 0;
        while (matcher.find()) {
            VM_Name[index] = matcher.group(1);
            index++;
        }
//        System.out.println(Arrays.toString(uuid_temp));
//        System.out.println(Arrays.toString(VM_Name));
        
        for (int i = 0; i < this.Max_VM; i++) {
            if (VM_Name[i]!=null){
            this.VMNameMap.put(VM_Name[i],uuid_temp[i]);
            this.VMNumberMap.put(i,VM_Name[i]);
            }
            else {
                this.NoOfVM = i-1; //this is becasue this list also includes the control domain VM id
                break;
            }
        }
        
        System.out.println(this.VMNameMap);
        System.out.println(this.VMNumberMap);
        //System.out.println(this.VMNameMap.get("server1"));
        
        
    }
    

    public void setFreq(int freq) throws JSchException {
        String command = "xenpm set-scaling-speed " + freq*1000+ " \n";
        String serverFeedback = null;
        serverFeedback = sshConnection.sendCommand(this.session,command);
        if (serverFeedback !=null){
            //System.out.print("\n Frequency of Server " + this.host +" Changed to " + freq + " MHz");
        }
     }
    
    public void setFreq(int freq, int core) throws JSchException {
        // run theis command in all XenServer once to enable use control "xenpm set-scaling-governor userspace"
        String command = "xenpm set-scaling-speed " + core + " " + freq*1000+ " \n";
        String serverFeedback = null;
        serverFeedback = sshConnection.sendCommand(this.session,command);
        if (serverFeedback != null) {
            //System.out.print("\n Frequency of Server " + this.host + " Changed to " + freq + " MHz for core "+core);
        }
    }

    public ArrayList getFreqAavailabe() throws JSchException {
        
        String command = "xenpm get-cpufreq-states 0 \n";
        String serverFeedback = null;
        serverFeedback = sshConnection.sendCommand(this.session,command);
        
        Scanner scanner = new Scanner(serverFeedback);//new java.io.File("results.txt"));
       
        String result = scanner.findWithinHorizon("\\[\\d+\\s+MHz\\]", 0);
        ArrayList<Long> speeds = new ArrayList<Long>();

        Pattern pattern = Pattern.compile("\\d+");
        while (result != null) {
            Matcher matcher = pattern.matcher(result);
            matcher.find();
            // This command returns MHz but when we set the speed, we need to
            // set them in Hz.
            Long l = Long.valueOf(matcher.group()) * 1000;
            speeds.add(l);
            result = scanner.findWithinHorizon("\\[\\d+\\s+MHz\\]", 0);
        }
        
        Collections.sort(speeds);
        //System.out.println("Number of CPU Speeds for: "+host+"  "+speeds.size());
        //System.out.println(speeds);

        //System.out.print(serverFeedback);
        return speeds;

    }
    
    public int getFreqCurrent(int core) throws JSchException {
        
        String command = "xenpm get-cpufreq-states "+ core +" \n";
        String serverFeedback = null;
        serverFeedback = sshConnection.sendCommand(this.session,command);
        
        Pattern pattern = Pattern.compile("current frequency    : (.*?) MHz");
        Matcher matcher = pattern.matcher(serverFeedback);
        int currentFrequency=0;
        if (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            currentFrequency = i;
        }
           return currentFrequency;

    }
    
    
    public int[] getFreqCurrent() throws JSchException {

        int currentFrequency[];
        currentFrequency = new int[NoOfCore];
        String command = "xenpm get-cpufreq-states \n";
        String serverFeedback;
        serverFeedback = sshConnection.sendCommand(this.session,command);

        Pattern pattern = Pattern.compile("current frequency    : (.*?) MHz");
        Matcher matcher = pattern.matcher(serverFeedback);
        int index = 0;
        while (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            currentFrequency[index] = i;
            index++;
        }
        return currentFrequency;

    }
    
    public double getUtilization(int core) throws JSchException{
        String command =  "xe host-cpu-param-get uuid="+cpu_uuid[core]+" param-name=utilisation \n";
        String serverFeedback;
        serverFeedback = sshConnection.sendCommand(this.session,command);
        
        System.out.println(serverFeedback);
        
        double utilization = 0.0;
        Pattern pattern = Pattern.compile("param-name=utilisation \r\n(.*?)\r\n");
        Matcher matcher = pattern.matcher(serverFeedback);
        if (matcher.find()) {
            utilization = Double.parseDouble(matcher.group(1))*100;
        }
       
        return utilization;
        
     
    }
    
    public double[] getUtilization() throws JSchException{
        double utilization[];
        utilization = new double[NoOfCore];
        
        for (int i = 0; i < NoOfCore; i++) {
            String command = "xe host-cpu-param-get uuid=" + cpu_uuid[i] + " param-name=utilisation \n";
            String serverFeedback;
            serverFeedback = sshConnection.sendCommand(this.session, command);
            Pattern pattern = Pattern.compile("param-name=utilisation \r\n(.*?)\r\n");
            Matcher matcher = pattern.matcher(serverFeedback);
            if (matcher.find()) {
                utilization[i] = Double.parseDouble(matcher.group(1)) * 100;
            }

        }
//        System.out.println("\n Server utilization = "+utilization*100+" %");
        
        return utilization;
    }
    
    public void setWeight(int VM_number, int weight) throws JSchException {
        String uuid = (String) this.VMNameMap.get(this.VMNumberMap.get(VM_number));
        String command = "xe vm-param-set uuid=" + uuid + " VCPUs-params:weight=" + weight + " \n";
        String serverFeedback = sshConnection.sendCommand(this.session, command);

    }

    
    public void setWeight(String VM_name, int weight) throws JSchException {
        String uuid = (String) this.VMNameMap.get(VM_name);
        String command = "xe vm-param-set uuid=" + uuid + " VCPUs-params:weight=" + weight + " \n";
        String serverFeedback = sshConnection.sendCommand(this.session, command);

    }
    
    public void setWeight(int weight) throws JSchException {
        
        for (int i = 0; i < this.NoOfVM; i++) {
            String uuid = (String) this.VMNameMap.get(this.VMNumberMap.get(i));
            String command = "xe vm-param-set uuid=" + uuid + " VCPUs-params:weight=" + weight + " \n";
            String serverFeedback = sshConnection.sendCommand(this.session, command);
            
        }
    }
    
    
    public void setCap(int VM_number, int cap) throws JSchException {
        String uuid = (String) this.VMNameMap.get(this.VMNumberMap.get(VM_number));
        String command = "xe vm-param-set uuid=" + uuid + " VCPUs-params:cap=" + cap + " \n";
        String serverFeedback = sshConnection.sendCommand(this.session, command);

    }

    public void setCap(String VM_name, int cap) throws JSchException {
        String uuid = (String) this.VMNameMap.get(VM_name);
        String command = "xe vm-param-set uuid=" + uuid + " VCPUs-params:cap=" + cap + " \n";
        String serverFeedback = sshConnection.sendCommand(this.session, command);

    }

    public void setCap(int cap) throws JSchException {

        for (int i = 0; i < this.NoOfVM; i++) {
            String uuid = (String) this.VMNameMap.get(this.VMNumberMap.get(i));
            String command = "xe vm-param-set uuid=" + uuid + " VCPUs-params:cap=" + cap + " \n";
            String serverFeedback = sshConnection.sendCommand(this.session, command);

        }
    }
    
    
    public void setWeightCap(int weight, int cap) throws JSchException {

        for (int i = 0; i < this.NoOfVM; i++) {
            String uuid = (String) this.VMNameMap.get(this.VMNumberMap.get(i));
            String command = "xe vm-param-set uuid=" + uuid + " VCPUs-params:weight=" + weight + " \n";
            String serverFeedback = sshConnection.sendCommand(this.session, command);

        }

        for (int i = 0; i < this.NoOfVM; i++) {
            String uuid = (String) this.VMNameMap.get(this.VMNumberMap.get(i));
            String command = "xe vm-param-set uuid=" + uuid + " VCPUs-params:cap=" + cap + " \n";
            String serverFeedback = sshConnection.sendCommand(this.session, command);
        }
    }   
    
    
    public void setMask(int VM_number, int core_number) {

    }
    

}
