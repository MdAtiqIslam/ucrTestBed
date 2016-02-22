/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commonmodules;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 *
 * @author moislam
 */
public class pduPowerMeter {

    private String user = "readonly";
    private String password = "testbed";
    private String host = "192.168.137.32";
    private String command = "";
    private int noOfPorts;
    private int ports[];
    private int noOfReadings;
    private String serverFeedback = null;
    private HashMap<Integer, Integer> hmap = new HashMap<>();

    private final double samplingFreq = 1; //sample per seconds
    private final double loggingDuration;
    private pduMeterReading[] meterReadings;
    private int logId;

    public pduPowerMeter(int[] ports, int loggingDuration) {
        this.loggingDuration = loggingDuration;
        this.ports = ports;
        this.noOfPorts = ports.length;
        for (int i = 0; i < noOfPorts; i++) {
            hmap.put(this.ports[i], i);
        }
        noOfReadings = (int) (this.loggingDuration / samplingFreq);
        meterReadings = new pduMeterReading[noOfReadings];
        for (int i=0; i<noOfReadings;i++){
            meterReadings[i]=new pduMeterReading(noOfPorts);
        }

        // set the commnad string for the PDU based on sampling rate and duration
        String portString = "";
        for (int i = 0; i < noOfPorts; i++) {
            portString = portString + ports[i];
            if (i < noOfPorts - 1) {
                portString += ",";
            }
        }
        String logCommand = "olReading " + portString + " power \n";
        //String logCommand = "olReading all power \n";
        String dateCommand = "date \n";

        for (int i = 0; i < this.loggingDuration * this.samplingFreq *8; i++) {
            //this.command += (dateCommand+logCommand);
            //without the date commnad, the date command does not work with "readonly user"
            this.command += logCommand;
        }
    }
    
    public void setLogId(int logId){
        this.logId=logId;
    }

    public void resetReadings(){
        for (int i=0; i<noOfReadings; i++){
            for (int j=0; j<noOfPorts;j++){
                meterReadings[i].readings[j]=0;
            }
        }
    }
    
    public void startLogging() throws JSchException, InterruptedException, IOException {
        sshModule sshPDU = new sshModule(host, user, password);
        Session session = sshPDU.startSession();
        serverFeedback = sshPDU.sendCommandPDU(session, command, samplingFreq, loggingDuration);
        sshPDU.stopSession();
        resetReadings();
        //System.out.println(serverFeedback);
        extractReading();
        responseSummarizer();
        logToFile();
    }

    private void extractReading() {
        int readingIndex = -1;
        String lines[] = this.serverFeedback.split("\\r?\\n");
        double[] sumReading = new double[noOfPorts];
        int[] readingCount = new int[noOfPorts];
        for (int i = 0; i < noOfPorts; i++) {
            sumReading[i] = 0.0;
            readingCount[i] = 0;
        }

        for (String line : lines) {
            String lineBreaks[] = line.split(":");
            if (lineBreaks[0].matches("Response time")) {
                
                if (readingIndex > -1) {
                    for (int i = 0; i < noOfPorts; i++) {
                        if (readingCount[i] > 0) {
                            //meterReadings[readingIndex].setReading(i, sumReading[i] / readingCount[i]);
                            meterReadings[readingIndex].readings[i]= sumReading[i] / readingCount[i];
                        }
                        sumReading[i] = 0.0;
                        readingCount[i] = 0;
                    }
                }
                
                readingIndex++;
                if (readingIndex>noOfReadings-1)
                    break;
                //meterReadings[readingIndex].setTimeStamp(line);
                meterReadings[readingIndex].timeStamp=line;
            }

            if (isNumeric(lineBreaks[0]) && lineBreaks.length==3) {
                int port = Integer.parseInt(lineBreaks[0]);
                String lineBreaks2[] = lineBreaks[2].split("\\s+");
                int reading = Integer.parseInt(lineBreaks2[1]);
                int portIndex = hmap.get(port);
                sumReading[portIndex] += reading;
                readingCount[portIndex] += 1;
            }

        }

    }

    public static boolean isNumeric(String str) {
        return str.matches("\\s?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    
    public void responseSummarizer(){
        double[] average = new double[noOfPorts];
        double[] max = new double[noOfPorts];
        for (int i=0; i<noOfPorts;i++){
            average[i]=0; max[i]=0;
        }
        
        for (int i=0;i<noOfPorts;i++){
            int readingCount=0;
            for(int j=0; j<noOfReadings;j++){
                double currentReading = meterReadings[j].readings[i];
                if (currentReading !=0) {average[i] += currentReading; readingCount++;}
                if (currentReading>max[i]) max[i]=currentReading;
            }
            if (readingCount>0) average[i] = average[i]/readingCount;
            else average[i]=0;
        }
        for (int i=0;i<noOfPorts;i++){
            System.out.print("Port "+ports[i]+"- Avg:"+average[i] + ", Max:"+max[i]+",");
        }
        
        
    }
    
    public void logToFile() throws IOException{
        FileWriter writer = new FileWriter("C:\\local_files\\files\\output\\websearch\\power\\"+logId+"_"+System.currentTimeMillis()+".csv");
        for (int i=0; i<noOfPorts;i++){
            writer.append(String.valueOf("Port:"+ports[i]));
            writer.append(',');
        }
        writer.append('\n');
        for (int i = 0; i < noOfReadings; i++) {
            for (int j=0; j<noOfPorts;j++){
                writer.append(String.valueOf(meterReadings[i].readings[j]));
                writer.append(',');
            }
            writer.append('\n');
        }

        writer.flush();
        writer.close();
        
    }

}
