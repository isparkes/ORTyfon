#!/bin/bash

DIR_ROOT=/opt/ORTyfon
DIR_LOGS=$DIR_ROOT/log
DIR_DATA=$DIR_ROOT/Data/Tyfon
DIR_TEMP=/tmp

DATE_NOW=`date +%Y%m%d-%H%M%S`

LOG_NAME="FTPGET-${DATE_NOW}.log"
LOG_FILE=$DIR_LOGS/$LOG_NAME

FTP_HOST=ftp6.web04.ux.tyfon.net
FTP_USER=ventelo
FTP_PASS=xbpfb2

COMMAND_FILE=$DIR_TEMP/ftpcommandfile
FETCH_LIST=$DIR_TEMP/filelist.txt

rm -f $COMMAND_FILE $FETCH_LIST

# get the list of files on the FTP-server
echo "open $FTP_HOST"           >  $COMMAND_FILE
echo "user $FTP_USER $FTP_PASS" >> $COMMAND_FILE
echo "cd CDR"                   >> $COMMAND_FILE
echo "ls"                       >> $COMMAND_FILE
echo "bye"                      >> $COMMAND_FILE
echo "quit"                     >> $COMMAND_FILE

# get the file list
ftp -inv < ${COMMAND_FILE} > $FETCH_LIST 

# exit if there are no files to fetch
if ! grep -q '\.txt' $FETCH_LIST >/dev/null ; then
   exit
fi

# create the command list for the retrieve
echo "open $FTP_HOST"           >  $COMMAND_FILE
echo "user $FTP_USER $FTP_PASS" >> $COMMAND_FILE
echo "cd CDR"                   >> $COMMAND_FILE
awk '/IP(ORG|_VCP)_/&&/\.txt/{
   print "get",$9,$9".wait";
   print "delete",$9;
}' $FETCH_LIST                  >> $COMMAND_FILE
echo "bye"                      >> $COMMAND_FILE
echo "quit"                     >> $COMMAND_FILE

# retrieve the list of files 
cd $DIR_DATA
ftp -inv < ${COMMAND_FILE} >> $LOG_FILE

# rename the files for processing
for file in `ls *.txt.wait 2>/dev/null` ; do
  file_nowait=`echo $file | sed 's/\.wait$//'`
  echo "Moving file $file to $file_nowait" >> $LOG_FILE
  mv $file $file_nowait
done
