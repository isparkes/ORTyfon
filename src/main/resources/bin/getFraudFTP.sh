INPUTDIR=/opt/ORTyfon/Data/Fraud
ARCHIVEDIR=$INPUTDIR/done

TARNAME="FraudTraffic-`date +%Y%m%d-%H%M%S`.tar.bz2"
DELETEFILENAME="DeleteFile-`date +%Y%m%d-%H%M%S`"

FILESPEC="transcoder*\.csv"

cd $INPUTDIR

echo "mget $FILESPEC" >> commandfile
echo "exit" >> commandfile

# Get the files
ftp -b commandfile cdruser:jNpWpYMY@ftp6.web04.ux.tyfon.net

exit 0

# See if we got any files
FILECOUNT=`ls $FILESPEC | wc -l` > /dev/null

echo "Got $FILECOUNT files"

if [ $FILECOUNT == "0" ]; then
  exit 0
fi

#tar up all the files we can find
find . -name "$FILESPEC*" | xargs tar --create --bzip2 --verbose --file $ARCHIVEDIR/$TARNAME

#echo "Deleting Archived Files"
if [ $? == 0 ]; then
  # delete the files that made the archive

  tar tvfj $ARCHIVEDIR/$TARNAME | awk '{print "rm "$6}' >> $DELETEFILENAME
  tar tvfj $ARCHIVEDIR/$TARNAME | awk '{print $6}' | xargs rm

  echo "done OK"
  echo "exit" >> $DELETEFILENAME
else
  echo "tar file creation failure"
fi

# delete the files from the ftp server
ftp -b $DELETEFILENAME cdruser:jNpWpYMY@ftp6.web04.ux.tyfon.net

