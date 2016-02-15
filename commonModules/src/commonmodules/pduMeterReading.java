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
public class pduMeterReading {
    public String timeStamp;
    public double[] readings;
    
    public pduMeterReading(int noOfPorts){
        this.timeStamp="";
        this.readings = new double[noOfPorts];
        for (int i=0; i<readings.length;i++){
            readings[i]=0;
        }
    }
    
    public void setTimeStamp(String timeStamp){
        this.timeStamp = timeStamp;
    }
    
    public void setReading (int index, double reading){
        this.readings[index]=reading;
    }
    
}
