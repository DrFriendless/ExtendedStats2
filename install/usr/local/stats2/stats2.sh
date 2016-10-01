#! /bin/sh

export PORT=8082
ARGS=/usr/local/stats2/mysql.properties
DAEMON="/usr/java/latest/bin/java -jar /usr/local/stats2/stats2.jar"
$DAEMON $ARGS