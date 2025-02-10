package com.softwaredesign.project.command;

public interface OrderCommand {
    void execute();
    void undo();
}
