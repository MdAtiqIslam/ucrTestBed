/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commonmodules;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author moislam
 */
public class pduPowerMeter {

    private String user = "readonly";
    private String password = "testbed";
    private String host = "169.235.14.147";
    private String command = "";
    private int noOfPorts;
    private int ports[];
    private int noOfReadings;
    private String serverFeedback = null;
    private HashMap<Integer, Integer> hmap = new HashMap<>();

    private final double samplingFreq = 1; //sample per seconds
    private final double loggingDuration;
    private pduMeterReading[] meterReadings;

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

        for (int i = 0; i < this.loggingDuration * this.samplingFreq * 10; i++) {
            //this.command += (dateCommand+logCommand);
            //without the date commnad, the date command does not work with "readonly user"
            this.command += logCommand;
        }
    }

    public void startLogging() throws JSchException, InterruptedException, IOException {
        sshModule sshPDU = new sshModule(host, user, password);
        Session session = sshPDU.startSession();
        serverFeedback = sshPDU.sendCommandPDU(session, command, samplingFreq, loggingDuration);
        sshPDU.stopSession();
        System.out.println(serverFeedback);
        extractReading();
        int i=0;
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

}
