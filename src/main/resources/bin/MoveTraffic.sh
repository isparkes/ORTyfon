#!/bin/bash

LOCKFILE=/opt/ORTyfon/Data/Tyfon/lock
INCDRSTAGINGDIR=/opt/ORTyfon/Data/Tyfon
OUTCDRDIR=/xmlroot/callFiles/in
INPROVSTAGINGDIR=/xmlroot/voipAccountInformationFiles/out
OUTPROVDIR=/opt/ORTyfon/Data/Tyfon/Provisioning

# check for the lock
if [ -f $LOCKFILE ]; then
  echo "lockfile exists, skipping"
  exit 0
fi

#change the permissions on the files, excluding tmp files
cd $INCDRSTAGINGDIR
for file in `ls callfile-*.xml 2>/dev/null`; do
  echo "Moving rated callfile $file"
  chmod 666 $file
  mv $file $OUTCDRDIR
done

cd $INPROVSTAGINGDIR
for file in `ls voipAccountInformation*.xml 2>/dev/null`; do
  echo "Moving provioning file $file"
  # chmod 666 $file
  mv $file $OUTPROVDIR
done

