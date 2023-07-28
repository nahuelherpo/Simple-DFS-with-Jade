#!/bin/bash
#
# Run example
#
# Author: Herpo Nahuel
#
# Date: Oct, 2022
#
#


if [ $# -eq 0 ] || [ $# -eq 1 ]; then
	echo "You must indicate the operation on the DFS and the file path"
	exit 0
else
	if [ $1 = "-r" ] || [ $1 = "-w" ]; then
		OPERATION=$1
		echo ${OPERATION}
	else
		echo "Error bad arguments"
		exit 0
	fi
fi
echo "Compiling"
javac -classpath lib/jade.jar -d classes myexamples/AgenteMovil.java
#echo "Up the main container"
java -cp lib/jade.jar:classes jade.Boot -gui -container-name ServerDFS &
sleep 5
echo "Upping the others container"
for container in DataServer1 DataServer2; do
	java -cp lib/jade.jar:classes jade.Boot -gui -container -container-name "${container}" -local-host localhost &
	sleep 2
done;
java -cp lib/jade.jar:classes jade.Boot -gui -container -container-name Client \
-local-host localhost -agents "myAgent:AgenteMovil(${OPERATION},$2)"
