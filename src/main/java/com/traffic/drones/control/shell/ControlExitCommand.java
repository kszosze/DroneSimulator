package com.traffic.drones.control.shell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

@ShellComponent
public class ControlExitCommand implements Quit.Command {

    @ShellMethod(value = "Exit the shell.", key = {"quit", "exit"})
    public void quit() {
        System.exit(0);
    }
}
