TYFON_EMAIL=mn@tyfon.net
OPENRATE_EMAIL=isparkes@tsm-consulting.de,ian.sparkes@gmail.com

# Script to monitor error logs and traffic loaded. Must run after midnight to give correct results for the
# traffic loaded statistics for yesterday, but should show the results of the most recent
# recycling. Therefore this script reports from the database for yesterday, but reports errors for
# today, under the assumption that the recycling runs after midnight.
cd /opt/ORTyfon/Data/Tyfon
rm mail.txt
YESTERDAY_DATE=`date '+%Y-%m-%d' --date='yesterday'`
RUN_DATE=`date '+%Y-%m-%d'`

#Get the rejection logs
echo "Rejections for $RUN_DATE" >> mail.txt
cat *.reject | grep ERR_CUST_NOT_FOUND | awk -F";" '{print $6}' | sort | uniq -c >> mail.txt

# Separator
echo "----------------------------" >> mail.txt

# Fraud statistics
echo "Fraud alerts for $YESTERDAY_DATE" >> mail.txt
cat /opt/ORTyfon/log/FraudPipe.log | grep $YESTERDAY_DATE | grep FRAUD >> mail.txt

# Separator
echo "----------------------------" >> mail.txt

# Send the mail
mailx -s "[Tyfon] OpenRate Traffic Report $RUN_DATE" -r openrate@tyfon.net -c $OPENRATE_EMAIL $TYFON_EMAIL < mail.txt

