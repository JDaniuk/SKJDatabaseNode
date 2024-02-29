@echo off
rem Start 3 network nodes, then terminate them
start java DatabaseNode -tcpport 9000 -record 1:1 
timeout 1 > NUL
start java DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:2 
timeout 1 > NUL
start java DatabaseNode -tcpport 9002 -connect localhost:9000 -connect localhost:9001 -record 3:3
timeout 1 > NUL
java DatabaseClient -gateway localhost:9002 -operation set-value 1:10
java DatabaseClient -gateway localhost:9000 -operation set-value 2:20
java DatabaseClient -gateway localhost:9001 -operation set-value 3:30
java DatabaseClient -gateway localhost:9001 -operation set-value 5:50
java DatabaseClient -gateway localhost:9002 -operation get-value 2
java DatabaseClient -gateway localhost:9000 -operation get-value 3
java DatabaseClient -gateway localhost:9001 -operation get-value 1
java DatabaseClient -gateway localhost:9001 -operation get-value 10
java DatabaseClient -gateway localhost:9001 -operation set-value 3:32
pause
java DatabaseClient -gateway localhost:9002 -operation terminate
java DatabaseClient -gateway localhost:9001 -operation terminate
java DatabaseClient -gateway localhost:9002 -operation terminate
java DatabaseClient -gateway localhost:9000 -operation terminate