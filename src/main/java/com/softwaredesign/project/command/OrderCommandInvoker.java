package com.softwaredesign.project.command;

import java.util.Stack;

public class OrderCommandInvoker {
    private final Stack<OrderCommand> commandHistory = new Stack<>();
    
    public void executeCommand(OrderCommand command) {
        command.execute();
        commandHistory.push(command);
    }
    
    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            OrderCommand command = commandHistory.pop();
            command.undo();
        }
    }
    
    public void clearHistory() {
        commandHistory.clear();
    }
}
