/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commonmodules;

/**
 *
 * @author moislam
 */
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class sshModule {

    private String user;// = "root"; // TODO: Username of ssh account on remote machine
    private String host; // = "10.80.18.1"; // TODO: Hostname of the remote machine (eg: inst.eecs.berkeley.edu)
    private String password; // = "SCGServer@15"; // TODO: Password associated with your ssh account
    //private static final String command = "xenpm get-cpufreq-states 0 \n"; // Remote command you want to invoke
    //private static final String command2 = "xenpm get-cpufreq-states 1 \n"; // Remote command you want to invoke
    //private static final long TIME_OUT = 10000;
    private static Session session;
    //private static Channel channel;
    //private InputStream is;

    public sshModule(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    public Session startSession() throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(10 * 1000);
        return session;
    }

    public void stopSession() throws JSchException {
        session.disconnect();
    }

    public String sendCommandPDU(Session session, String command, double samplingRate, double loggingDuration) throws JSchException, InterruptedException, IOException {

        Channel channel = session.openChannel("shell");
        InputStream is = new ByteArrayInputStream(command.getBytes());

        channel.setInputStream(is);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        channel.setOutputStream(output);

        channel.connect(5000);
        String aString = null;
        String feedBack = "";
        for (int i = 0; i < 1 + loggingDuration / samplingRate; i++) {
            try {
                Thread.sleep((long) (samplingRate * 1000));
            } catch (InterruptedException ex) {
                Logger.getLogger(sshModule.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                aString = new String(output.toByteArray(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(sshModule.class.getName()).log(Level.SEVERE, null, ex);
            }

            output.reset();
            if (i > 0) {
                //System.out.print(aString);
                SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
                String current_time_str = time_formatter.format(System.currentTimeMillis());
                //System.out.println("Response time:" + current_time_str);
                feedBack += "\nResponse time:" + current_time_str + "\n";
                feedBack += aString;
            }
        }
        is.reset();
        channel.disconnect();
        return feedBack + "\nResponse time: finish";
    }

    public String sendCommand(Session session, String command) throws JSchException {

        Channel channel = session.openChannel("shell");
        InputStream is = new ByteArrayInputStream(command.getBytes());
        channel.setInputStream(is);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        channel.setOutputStream(output);
        channel.connect(5000);
        String aString = "";
        try {
            Thread.sleep((long) (5 * 1000));
        } catch (InterruptedException ex) {
            Logger.getLogger(sshModule.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            aString = new String(output.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(sshModule.class.getName()).log(Level.SEVERE, null, ex);
        }

        channel.disconnect();
        return aString;
    }

    public String sendCommandWcheck(Session session, String command, int timeOut) throws JSchException {

        Channel channel = session.openChannel("shell");
        InputStream is = new ByteArrayInputStream(command.getBytes());
        channel.setInputStream(is);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        channel.setOutputStream(output);
        channel.connect(5000);
        String serverFeedback = "";
        boolean jobDone = false;
        int loopCount = 0;
        int timeOutCount=timeOut*60/5;
        while (!jobDone) {
            loopCount++;
            
            String aString = "";
            try {
                Thread.sleep((long) (5 * 1000));
            } catch (InterruptedException ex) {
                Logger.getLogger(sshModule.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                aString = new String(output.toByteArray(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(sshModule.class.getName()).log(Level.SEVERE, null, ex);
            }
            output.reset();

            String lines[] = aString.split("\\r?\\n");

            for (String line : lines) {
                if (line.matches("(?i).*completed successfully.*")) {
                    jobDone = true;
                    aString+="\n Job done detected";
                    break;
                }
                if (line.matches("(?i).*terasort.TeraSort: done.*")) {
                    jobDone = true;
                    aString+="\n Job done detected";
                    break;
                }
            }
            serverFeedback += aString;
            if(loopCount>timeOutCount) {
                System.out.println("Warning!!!! Check timed out. Job end not detected!!!!");
                break;
            }
        }

        channel.disconnect();
        return serverFeedback;
    }

}
