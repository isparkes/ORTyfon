#!/bin/bash
export OR_HOME=/opt/ORTyfon

# Mysql information
export MYSQL_USER=openrate
export MYSQL_PASS=openrate
export MYSQL_DB=TyfonDB

cd $OR_HOME/db-backups
DBBACKUPTARNAME="OR-DB-Backup-Tyfon-`date +%F`.sql"

#do the dump
mysqldump --user=$MYSQL_USER --password=$MYSQL_PASS --routines $MYSQL_DB > $DBBACKUPTARNAME

#Compress it
bzip2 $DBBACKUPTARNAME

exit 0
