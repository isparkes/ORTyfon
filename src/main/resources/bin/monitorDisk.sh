TYFON_EMAIL=mn@tyfon.net
OPENRATE_EMAIL=isparkes@tsm-consulting.de,ian.sparkes@gmail.com
BLOCKSROOT=`df -P | grep "/dev/sda1" | awk '{print $4}'`
BLOCKSXML=`df -P | grep "lnx1:/var/share/samba/kunder/openRate" | awk '{print $4}'`

if [ -f mail.txt ]; then 
  rm mail.txt
fi

if [ $BLOCKSROOT -lt "4000000" ]; then
  # Notify
  echo "Disk space on OpenRate root file system less than 4GB" >> mail.txt
fi

if [ $BLOCKSXML -lt "4000000" ]; then
  # Notify
  echo "Disk space on OpenRate transfer mount file system less than 4GB" >> mail.txt
fi

if [ -f mail.txt ]; then
  mailx -s "[Tyfon] Diskspace alert test " -r openrate@tyfon.net -c $OPENRATE_EMAIL $TYFON_EMAIL < mail.txt
fi
