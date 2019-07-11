# Control Drone

### Description
Drone simulation system to probe the behaviour of queues in multithreading
environment. Testing for Spring Shell environment.

### Build
Following instructions are to build the project

```
$ mvn clean install
```

### Execution
In the folder Target create after build there will be a jar file.
In order to run it following the next command.
```
$ java -jar control-0.0.1-SNAPSHOT.jar
```

### Instuctions
Once the application is running a prompt will appear
```
shell:>
```
typing help in whatever moment we can see the list of available commands
```
shell:> help

Built-In Commands
        clear: Clear the shell screen.
        exit, quit: Exit the shell.
        help: Display help about available commands.
        history: Display or save the history of previously run commands
        script: Read and execute commands from a file.
        stacktrace: Display the full stacktrace of the last error.

Control Room
        autonomous-fly: Start automatic fly mode
        launch: Launch drones
        locate-drones: List actual Drones Positions
        send-drone-to: Send position to drone -  droneId, longitude, latitude
        shutdown: Shutdown drones

```

_Control Room_ commands are the one implemented in this application.

They must run in an specific order:
* Launch - will create and start the threads simulating the drones
* autonomous-fly - Will read the files with coordinates and send them to the Drones and reaching the shutdown time will turn them off. Communications wil be log in the console
* shutdown - will send the shutdown order in case we fly in manual mode.
* send-drone-to - will send a position to fly to the designated drone.
* locate-drones - print in console the position of the drones.
