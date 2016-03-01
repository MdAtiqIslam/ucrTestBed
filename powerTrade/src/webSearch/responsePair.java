/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSearch;

/**
 *
 * @author scgclient
 */
public class responsePair {
    
    private long reqTime;
    private long responseTime;
    
   public responsePair(long reqTime, long responseTime){
        this.reqTime = reqTime;
        this.responseTime = responseTime;
    }
    
    public void setData(long reqTime, long responseTime){
        this.reqTime = reqTime;
        this.responseTime = responseTime;
    }
    
    public long[] getData() {
        long[] data = new long[2];
        data[0] = this.reqTime;
        data[1] = this.responseTime;
        return data;
    }
    
}
