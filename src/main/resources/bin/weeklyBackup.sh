#!/bin/bash

#Perform weekly offsite backup
cd /opt/ORTyfon/db-backups
DBBACKUPTARNAME="OR-DB-Backup-Tyfon-`date +%F`.sql.bz2"
RUN_DATE=`date +%F`
TYFON_EMAIL=mn@tyfon.net
OPENRATE_EMAIL=ian@open-rate.com

#encrypt the file
export PATH=$PATH:/opt/ORTyfon/bin:/home/openrate/bin
echo "UFC9R5JD" > pp.txt
/usr/bin/gpg --no-tty --batch -c --passphrase-fd 3 $DBBACKUPTARNAME 3<pp.txt
rm pp.txt

ftpput $DBBACKUPTARNAME.gpg

# Notify
echo "Weekly backup $DBBACKUPTARNAME.gpg created on $RUN_DATE" > mail.txt
mailx -s "Weekly backup $DBBACKUPTARNAME.gpg created on $RUN_DATE" -r $TYFON_EMAIL $OPENRATE_EMAIL < mail.txt

