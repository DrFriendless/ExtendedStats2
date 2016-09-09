#! /bin/bash

# extract data from the full extended stats database.
ABS=`readlink -e $0`
DOT=`dirname $ABS`
/bin/rm -rf /tmp/data
/bin/mkdir /tmp/data
/bin/chmod 777 /tmp/data
/usr/bin/mysql --user=root --password=basilisk --database=extended <export.sql >/tmp/data/export.log
/bin/cat /tmp/data/export.log
cd /tmp/data
/usr/java/latest/bin/jar cf stats2db.jar *.csv
/bin/echo "Output is /tmp/data/stats2db.jar"
/bin/cp stats2db.jar $DOT/../res/data