package io.mybear.tracker.command;

import io.mybear.net2.tracker.TrackerConnection;
import io.mybear.net2.tracker.TrackerMessage;

public abstract class TrackerCommand {
    public abstract void handle(TrackerConnection conn, TrackerMessage message);
}
