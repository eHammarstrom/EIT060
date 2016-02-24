## HOW TO RUN

* Client CANNOT be started in Eclipse, Eclipse merely emulates a console/terminal.

1. Server can be started in eclipse or compiled, must pass PORT as argument.
2. Compile client in eclipse.
  1. Right-click client.java.
  2. Export as RUNNABLE JAR.
  3. Select the client to be the launch configuration.
3. Start client in the folder project_2/ with IP and PORT as arguments in the mentioned order.

> The server is compiled to a runnable JAR file in this scenario. May be executed in the eclipse environment.

```zsh
java -jar server.jar 6666
```
```zsh
java -jar client.jar localhost 6666
```
