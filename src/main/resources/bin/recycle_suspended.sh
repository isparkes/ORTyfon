#!/bin/bash

OR_DATA_DIR=/opt/ORTyfon/Data
RECYCLE_DIR_LIST="Tyfon"

# loop through all the directories in which we can find rejections
for subdir in $RECYCLE_DIR_LIST ; do
   workdir=$OR_DATA_DIR/$subdir

   for filepath in $workdir/*.reject ; do
      if [ ! -f $filepath ] ; then # skip non files
         echo " Skipping nonfile $filepath"
         continue
      fi
      if [ ! -s $filepath ] ; then # skip empty files
         echo " Skipping empty file $filepath"
         continue
      fi

      filename=`basename $filepath`
      filename_noreject=`echo $filename | sed 's/\.reject$//'`
      filename_prefix=`echo $filename_noreject | sed -E 's/_R[0-9]+$//'`

      if [ "$filename_noreject" != "$filename_prefix" ] ; then
         recycle_count=`echo $filename_noreject | sed -E 's/^.*_R([0-9]+)$/\1/'`
      else
         recycle_count=0
      fi

      case $filename in
      IP_VCP_*_R*.reject|IPORG_*_R*.reject)
         echo " File $filename has been recyled $recycle_count time(s)."
         ;;

      IP_VCP_*.reject|IPORG_*.reject)
         echo " File $filename hasn't yet been recyled."
         ;;

      *)
         echo " Skipping unknown file $filepath"
         continue
         ;;
      esac

      recycle_count=$((recycle_count + 1))
      new_filename=${filename_prefix}_R${recycle_count}.txt

      echo "  Moving $filepath into $workdir/$new_filename"
      mv $filepath $workdir/$new_filename
   done
done
