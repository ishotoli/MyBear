package io.mybear.tracker.command;

import io.mybear.net2.Connection;

public abstract class TrackerCommand {
    public abstract void handle(Connection conn);
}
