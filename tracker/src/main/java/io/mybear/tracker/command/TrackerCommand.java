package io.mybear.tracker.command;


import io.mybear.tracker.trackerNio.TrackerConnection;
import io.mybear.tracker.trackerNio.TrackerMessage;

public abstract class TrackerCommand {
    public abstract void handle(TrackerConnection conn, TrackerMessage message);
}
