#!/bin/bash
echo "Compiling"
javac -classpath lib/jade.jar -d classes myexamples/AgenteMovil.java
echo "Up the main container"
java -cp lib/jade.jar:classes jade.Boot -gui &
sleep 5
echo "Upping the others container"
for agents in 0 1 2; do
	java -cp lib/jade.jar:classes jade.Boot -gui -container -host localhost &
	sleep 2
done;
java -cp lib/jade.jar:classes jade.Boot -gui -container -host localhost -agents myAgent:AgenteMovil
echo "Finish"
