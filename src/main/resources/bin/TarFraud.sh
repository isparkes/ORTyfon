#!/bin/bash

LOCKFILE=/opt/ORTyfon/Data/Fraud/lock
INPUTDIR=/opt/ORTyfon/Data/Fraud
ARCHIVEDIR=/opt/ORTyfon/Data/Fraud/Archive

# check for the lock
if [ -f $LOCKFILE ]; then
  echo "lockfile exists, skipping"
  exit 0
fi

cd $INPUTDIR

TEMPFILE=/tmp/`basename $0`.$RANDOM
TARNAME="Fraud-`date +%Y%m%d-%H%M%S`.tar.bz2"

# --------------------------- Traffic -----------------------------------
#tar up all the files we can find
find . -name "*.done" -o -name "*.dump" > $TEMPFILE

if [ ! -s $TEMPFILE ] ; then
   # no files to tar up
   rm -f $TEMPFILE
   exit 0
fi

# cat $TEMPFILE | xargs tar --create --bzip2 --verbose --remove-files --file $ARCHIVEDIR/$TARNAME
cat $TEMPFILE | xargs tar --create --bzip2 --remove-files --file $ARCHIVEDIR/$TARNAME

if [ $? == 0 ]; then
  echo "Fraud done OK"
else
  echo "Fraud tar file creation failure"
fi

rm -f $TEMPFILE
