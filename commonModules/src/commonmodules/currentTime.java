/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commonmodules;

import java.text.SimpleDateFormat;

/**
 *
 * @author atiq
 */
public class currentTime {
    SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
    String current_time_str = time_formatter.format(System.currentTimeMillis());
    
    public void printCurrentTime(){
        System.out.print(this.current_time_str);
    }
    
    public String getCurrentTime(){
        return this.current_time_str;
    }
    
}
