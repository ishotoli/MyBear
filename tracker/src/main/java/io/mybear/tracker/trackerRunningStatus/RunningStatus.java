package io.mybear.tracker.trackerRunningStatus;

/**
 * Created by jamie on 2017/8/10.
 */
public class RunningStatus {
    public int running_time;     //running seconds, more means higher weight
    public int restart_interval; //restart interval, less mean higher weight
    public boolean if_leader;       //if leader

}
