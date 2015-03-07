#!/bin/bash

CONFPATH=`dirname $0`

LIST_TEMPLATE_FILE=${CONFPATH}/pullFraud.template
GET_TEMPLATE_FILE=${CONFPATH}/getFraudFile.template
DEL_TEMPLATE_FILE=${CONFPATH}/delFraudFile.template
COMMAND_FILE=${CONFPATH}/ftpcommandfile
FILE_LIST=${CONFPATH}/rawFileList
FILES_TO_PROCESS=${CONFPATH}/fileList
OPENRATE_INPUT=/opt/ORTyfon/Data/Fraud/
OPENRATE_INPUT_STAGING=/opt/ORTyfon/Data/Fraud/Staging/

cd ${OPENRATE_INPUT_STAGING}

date '+--- %Y-%m-%d %H:%M'

# get a list of files
for file in $COMMAND_FILE $FILE_LIST ; do
   [ -f $file ] && rm -f $file
done

cp $LIST_TEMPLATE_FILE $COMMAND_FILE

echo "Getting file list from FTP"
ftp -inv < ${COMMAND_FILE} >${FILE_LIST}

# process the raw file list
awk '/transcoder/{print $NF}' ${FILE_LIST} >${FILES_TO_PROCESS}

# end here if there's nothing to process
if [ ! -s $FILES_TO_PROCESS ] ; then
   echo "No files on FTP, exiting"
   exit
fi

# get the files
cat ${FILES_TO_PROCESS} | while read FILE ; do
  echo "Getting $FILE"

  sed "s/FILE_TO_GET/${FILE}/" ${GET_TEMPLATE_FILE} >${COMMAND_FILE}

  ftp -in >/dev/null < ${COMMAND_FILE}

  echo "Checking $FILE"
  if [ -s ${FILE} ] ; then
    echo "${FILE} file has been transferred, deleting from FTP"
    sed "s/FILE_TO_GET/${FILE}/" ${DEL_TEMPLATE_FILE} >${COMMAND_FILE}
    ftp -in >/dev/null < ${COMMAND_FILE}
  else
    echo "${FILE} file was NOT transferred from FTP"
    rm -f $FILE
  fi
done

# finally, move the files into the OpenRate folder
for file in `ls transcoder*.csv 2>/dev/null` ; do
   mv $file ${OPENRATE_INPUT}
done
