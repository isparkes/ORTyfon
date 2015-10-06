/* ====================================================================
 * Limited Evaluation License:
 *
 * The exclusive owner of this work is Tiger Shore Management Ltd.
 * This work, including all associated documents and components
 * is Copyright Tiger Shore Management Limited 2006-2012.
 *
 * The following restrictions apply unless they are expressly relaxed in a
 * contractual agreement between the license holder or one of its officially
 * assigned agents and you or your organisation:
 *
 * 1) This work may not be disclosed, either in full or in part, in any form
 *    electronic or physical, to any third party. This includes both in the
 *    form of source code and compiled modules.
 * 2) This work contains trade secrets in the form of architecture, algorithms
 *    methods and technologies. These trade secrets may not be disclosed to
 *    third parties in any form, either directly or in summary or paraphrased
 *    form, nor may these trade secrets be used to construct products of a
 *    similar or competing nature either by you or third parties.
 * 3) This work may not be included in full or in part in any application.
 * 4) You may not remove or alter any proprietary legends or notices contained
 *    in or on this work.
 * 5) This software may not be reverse-engineered or otherwise decompiled, if
 *    you received this work in a compiled form.
 * 6) This work is licensed, not sold. Possession of this software does not
 *    imply or grant any right to you.
 * 7) You agree to disclose any changes to this work to the copyright holder
 *    and that the copyright holder may include any such changes at its own
 *    discretion into the work
 * 8) You agree not to derive other works from the trade secrets in this work,
 *    and that any such derivation may make you liable to pay damages to the
 *    copyright holder
 * 9) You agree to use this software exclusively for evaluation purposes, and
 *    that you shall not use this software to derive commercial profit or
 *    support your business or personal activities.
 *
 * This software is provided "as is" and any expressed or impled warranties,
 * including, but not limited to, the impled warranties of merchantability
 * and fitness for a particular purpose are discplaimed. In no event shall
 * Tiger Shore Management or its officially assigned agents be liable to any
 * direct, indirect, incidental, special, exemplary, or consequential damages
 * (including but not limited to, procurement of substitute goods or services;
 * Loss of use, data, or profits; or any business interruption) however caused
 * and on theory of liability, whether in contract, strict liability, or tort
 * (including negligence or otherwise) arising in any way out of the use of
 * this software, even if advised of the possibility of such damage.
 * This software contains portions by The Apache Software Foundation, Robert
 * Half International.
 * ====================================================================
 */
package fraud;

import OpenRate.configurationmanager.ClientManager;
import OpenRate.exception.InitializationException;
import OpenRate.process.AbstractPersistentObjectProcess;
import OpenRate.record.IRecord;
import Tyfon.TyfonRecord;
import java.util.ArrayList;
import java.util.Set;

/**
 * This module calculates the keys that are used for performing the fraud
 * aggregations.
 *
 * @author ian
 */
public class FraudKeyCalculation extends AbstractPersistentObjectProcess {

  // List of Services that this Client supports
  private final static String SERVICE_PURGEKEYS = "PurgeKeys";

  private int monthlyTotalDurationLimit;
  private int dailyTotalDurationLimit;
//  private int hourlyTotalDurationLimit;

  private int monthlyCallTypeDurationLimit;
  private int dailyCallTypeDurationLimit;
//  private int hourlyCallTypeDurationLimit;

  private int monthlyTotalCountLimit;
  private int dailyTotalCountLimit;
  private int hourlyTotalCountLimit;

  private int monthlyCallTypeCountLimit;
  private int dailyCallTypeCountLimit;
  private int hourlyCallTypeCountLimit;

  @Override
  public void init(String PipelineName, String ModuleName) throws InitializationException {
    super.init(PipelineName, ModuleName);

    // **************************** Duration Total ***************************
    try {
      monthlyTotalDurationLimit = Integer.parseInt(getPropertyValue("MonthlyTotalDurationLimit"));
      getPipeLog().info("Using <" + monthlyTotalDurationLimit + "> for MonthlyTotalDurationLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <MonthlyTotalDurationLimit> not found", getSymbolicName());
    }

    try {
      dailyTotalDurationLimit = Integer.parseInt(getPropertyValue("DailyTotalDurationLimit"));
      getPipeLog().info("Using <" + dailyTotalDurationLimit + "> for DailyTotalDurationLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <DailyTotalDurationLimit> not found", getSymbolicName());
    }

//    try {
//      hourlyTotalDurationLimit = Integer.parseInt(getPropertyValue("HourlyTotalDurationLimit"));
//      getPipeLog().info("Using <" + hourlyTotalDurationLimit + "> for HourlyTotalDurationLimit");
//    } catch (NumberFormatException ex) {
//      throw new InitializationException("Intialisation parameter <HourlyTotalDurationLimit> not found",getSymbolicName());
//    }
    // *************************** Call types duration *************************
    try {
      monthlyCallTypeDurationLimit = Integer.parseInt(getPropertyValue("MonthlyCallTypeDurationLimit"));
      getPipeLog().info("Using <" + monthlyCallTypeDurationLimit + "> for MonthlyCallTypeDurationLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <MonthlyCallTypeDurationLimit> not found", getSymbolicName());
    }

    try {
      dailyCallTypeDurationLimit = Integer.parseInt(getPropertyValue("DailyCallTypeDurationLimit"));
      getPipeLog().info("Using <" + dailyCallTypeDurationLimit + "> for DailyCallTypeDurationLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <DailyCallTypeDurationLimit> not found", getSymbolicName());
    }

//    try {
//      hourlyCallTypeDurationLimit = Integer.parseInt(getPropertyValue("HourlyCallTypeDurationLimit"));
//      getPipeLog().info("Using <" + hourlyCallTypeDurationLimit + "> for HourlyCallTypeDurationLimit");
//    } catch (NumberFormatException ex) {
//      throw new InitializationException("Intialisation parameter <HourlyCallTypeDurationLimit> not found",getSymbolicName());
//    }
    // ****************************** Call Counts ******************************
    try {
      monthlyTotalCountLimit = Integer.parseInt(getPropertyValue("MonthlyTotalCountLimit"));
      getPipeLog().info("Using <" + monthlyTotalCountLimit + "> for MonthlyTotalCountLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <MonthlyTotalCountLimit> not found", getSymbolicName());
    }

    try {
      dailyTotalCountLimit = Integer.parseInt(getPropertyValue("DailyTotalCountLimit"));
      getPipeLog().info("Using <" + dailyTotalCountLimit + "> for DailyTotalCountLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <DailyTotalCountLimit> not found", getSymbolicName());
    }

    try {
      hourlyTotalCountLimit = Integer.parseInt(getPropertyValue("HourlyTotalCountLimit"));
      getPipeLog().info("Using <" + hourlyTotalCountLimit + "> for HourlyTotalCountLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <HourlyTotalCountLimit> not found", getSymbolicName());
    }

    // *************************** Call Count by type ****************************
    try {
      monthlyCallTypeCountLimit = Integer.parseInt(getPropertyValue("MonthlyCallTypeCountLimit"));
      getPipeLog().info("Using <" + monthlyCallTypeCountLimit + "> for MonthlyCallTypeCountLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <MonthlyCallTypeCountLimit> not found", getSymbolicName());
    }

    try {
      dailyCallTypeCountLimit = Integer.parseInt(getPropertyValue("DailyCallTypeCountLimit"));
      getPipeLog().info("Using <" + dailyCallTypeCountLimit + "> for DailyCallTypeCountLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <DailyCallTypeCountLimit> not found", getSymbolicName());
    }

    try {
      hourlyCallTypeCountLimit = Integer.parseInt(getPropertyValue("HourlyCallTypeCountLimit"));
      getPipeLog().info("Using <" + hourlyCallTypeCountLimit + "> for HourlyCallTypeCountLimit");
    } catch (NumberFormatException ex) {
      throw new InitializationException("Intialisation parameter <HourlyCallTypeCountLimit> not found", getSymbolicName());
    }
  }

  @Override
  public IRecord procValidRecord(IRecord r) {
    TyfonRecord CurrentRecord = (TyfonRecord) r;

    // We only transform the detail records, and leave the others alone
    if (CurrentRecord.RECORD_TYPE == TyfonRecord.FRAUD_DETAIL_RECORD) {

      // callDate has the date in it in the format yyyymmdd
      // Get the daily key
      String dailyKey = CurrentRecord.callDate.substring(0, 8);

      // Get the monthly key
      String monthlyKey = CurrentRecord.callDate.substring(0, 6);

      // Get the hourly key
      String hourlyKey = CurrentRecord.callDate.substring(0, 10);

      // Calcualate aggregations per customer
      String aggKeyCustomer = CurrentRecord.CustIDA + ".";

      String aggKeyCustomerMonthly = aggKeyCustomer + monthlyKey;
      String aggKeyCustomerDaily = aggKeyCustomer + dailyKey;
      String aggKeyCustomerHourly = aggKeyCustomer + hourlyKey;

      // **************************** Duration Total ***************************
      // The key for monthly aggregation
      String aggKeyCustomerTotalMonthly = aggKeyCustomerMonthly + ".TotDur";
      checkBalance(CurrentRecord, aggKeyCustomerTotalMonthly, CurrentRecord.callTime, monthlyTotalDurationLimit, "Monthly total duration");

      // The key for daily aggregation
      String aggKeyCustomerTotalDaily = aggKeyCustomerDaily + ".TotDur";
      checkBalance(CurrentRecord, aggKeyCustomerTotalDaily, CurrentRecord.callTime, dailyTotalDurationLimit, "Daily total duration");

//      // The key for hourly aggregation
//      String aggKeyCustomerTotalHourly = aggKeyCustomerHourly + ".TotDur";
//      checkBalance(CurrentRecord, aggKeyCustomerTotalHourly, CurrentRecord.callTime, hourlyTotalDurationLimit, "Hourly total duration");
      // ************************* Duration Call types *************************
      // The keys for the call type
      String callType = CurrentRecord.Dest_Phone_Type;
      if (CurrentRecord.isPremium) {
        callType = "Premium";
      }

      String aggKeyCustomerDestMonthly = aggKeyCustomerMonthly + ".Dur." + callType;
      checkBalance(CurrentRecord, aggKeyCustomerDestMonthly, CurrentRecord.callTime, monthlyCallTypeDurationLimit, "Monthly " + callType + " duration");

      String aggKeyCustomerDestDaily = aggKeyCustomerDaily + ".Dur." + callType;
      checkBalance(CurrentRecord, aggKeyCustomerDestDaily, CurrentRecord.callTime, dailyCallTypeDurationLimit, "Daily " + callType + " duration");

//      String aggKeyCustomerDestHourly = aggKeyCustomerHourly + ".Dur." + callType;
//      checkBalance(CurrentRecord, aggKeyCustomerDestHourly, CurrentRecord.callTime, hourlyCallTypeDurationLimit, "Hourly " + callType + " duration");
      // ***************************** Call Counts *****************************
      // The key for monthly aggregation
      String aggKeyCustomerCountMonthly = aggKeyCustomerMonthly + ".TotCnt";
      checkBalance(CurrentRecord, aggKeyCustomerCountMonthly, 1, monthlyTotalCountLimit, "Monthly total call count");

      // The key for daily aggregation
      String aggKeyCustomerCountDaily = aggKeyCustomerDaily + ".TotCnt";
      checkBalance(CurrentRecord, aggKeyCustomerCountDaily, 1, dailyTotalCountLimit, "Daily total call count");

      // The key for hourly aggregation
      String aggKeyCustomerCountHourly = aggKeyCustomerHourly + ".TotCnt";
      checkBalance(CurrentRecord, aggKeyCustomerCountHourly, 1, hourlyTotalCountLimit, "Hourly total call count");

      // ************************* Call Count by type **************************
      // The key for monthly aggregation
      String aggKeyCustomerDestCountMonthly = aggKeyCustomerMonthly + ".Cnt." + callType;
      checkBalance(CurrentRecord, aggKeyCustomerDestCountMonthly, 1, monthlyCallTypeCountLimit, "Monthly " + callType + " call count");

      // The key for daily aggregation
      String aggKeyCustomerDestCountDaily = aggKeyCustomerDaily + ".Cnt." + callType;
      checkBalance(CurrentRecord, aggKeyCustomerDestCountDaily, 1, dailyCallTypeCountLimit, "Daily " + callType + " call count");

      // The key for hourly aggregation
      String aggKeyCustomerDestCountHourly = aggKeyCustomerHourly + ".Cnt." + callType;
      checkBalance(CurrentRecord, aggKeyCustomerDestCountHourly, 1, hourlyCallTypeCountLimit, "Hourly " + callType + " call count");
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    // do nothing
    return r;
  }

  public void checkBalance(TyfonRecord CurrentRecord, String key, int eventImpact, int notificationLimit, String description) {
    // get the customer's balance from the cache
    FraudBalance balance = (FraudBalance) getObject(key);

    if (balance != null) {
      // deal with the large CDR case
      if (eventImpact >= notificationLimit) {
        // Update the CDR info
        balance.incFraudBalance(eventImpact);

        getPipeLog().error("FRAUD: Possible fraud warning for user <" + CurrentRecord.CustIDA + ">, counter: " + description + " key:" + key + " limit:" + notificationLimit);

        FraudAlert alert = new FraudAlert();
        alert.setBalance(balance.getFraudBalance());
        alert.setNotificationLimit(notificationLimit);
        alert.setThresholdName(key);
        alert.setDescription(description);

        CurrentRecord.alerts.add(alert);
      } else {
        // deal with the small impact accumulated to large balance case
        double dailyBalanceOld = balance.getFraudBalance();
        balance.incFraudBalance(eventImpact);
        double dailyBalanceNew = balance.getFraudBalance();

        if (Math.floor(dailyBalanceOld / notificationLimit) != Math.floor(dailyBalanceNew / notificationLimit)) {
          getPipeLog().error("FRAUD: Possible fraud warning for user <" + CurrentRecord.CustIDA + ">, counter: " + description + " key:" + key + " limit:" + notificationLimit);
          FraudAlert alert = new FraudAlert();
          alert.setBalance(balance.getFraudBalance());
          alert.setNotificationLimit(notificationLimit);
          alert.setThresholdName(key);
          alert.setDescription(description);

          CurrentRecord.alerts.add(alert);
        }
      }
    } else {
      // this is the first CDR for the user with the current date, store the balance
      balance = new FraudBalance(eventImpact);

      // store the balance
      putObject(key, balance);

      // Check the limit
      if (eventImpact >= notificationLimit) {
        getPipeLog().error("FRAUD: Possible fraud warning for user <" + CurrentRecord.CustIDA + ">, counter: " + description + " key:" + key + " limit:" + notificationLimit);
        FraudAlert alert = new FraudAlert();
        alert.setBalance(balance.getFraudBalance());
        alert.setNotificationLimit(notificationLimit);
        alert.setThresholdName(key);
        alert.setDescription(description);

        CurrentRecord.alerts.add(alert);
      }
    }
  }

  /**
   * Delete keys that match a given key template
   *
   * @param keyToPurge the key template we want to purge
   * @return The number of keys purged
   */
  private String purgeKeys(String keyToPurge) {
    long purgedCount = 0;
    long totalCount = 0;
    ArrayList<String> keysToPurge = new ArrayList<>();
    Set<String> keySet = getObjectKeySet();

    // Find the keys to purge (concurrent modifiation not allowed)
    for (String key : keySet) {
      totalCount++;
      if (key.contains(keyToPurge)) {
        keysToPurge.add(key);
      }
    }

    // Now do the delete
    for (String key : keysToPurge) {
      deleteObject(key);
      purgedCount++;
    }

    return "Purged " + purgedCount + " of " + totalCount + " keys using key <" + keyToPurge + ">";
  }

  /**
   * registerClientManager registers this class as a client of the ECI listener
   * and publishes the commands that the plug in understands. The listener is
   * responsible for delivering only these commands to the plug in.
   *
   * @throws OpenRate.exception.InitializationException
   */
  @Override
  public void registerClientManager() throws InitializationException {
    // Set up the parent
    super.registerClientManager();

    //Register services for this Client
    ClientManager.getClientManager().registerClientService(getSymbolicName(), SERVICE_PURGEKEYS, ClientManager.PARAM_DYNAMIC);
  }

  /**
   * processControlEvent is the event processing hook for the External Control
   * Interface (ECI). This allows interaction with the external world, for
   * example turning the dumping on and off.
   *
   * @param Command The command that we are to work on
   * @param Init True if the pipeline is currently being constructed
   * @param Parameter The parameter value for the command
   * @return The result message of the operation
   */
  @Override
  public String processControlEvent(String Command, boolean Init, String Parameter) {
    // Set the batch size
    if (Command.equalsIgnoreCase(SERVICE_PURGEKEYS)) {
      if (Parameter.isEmpty()) {
        return "Need to provide a parameter for <" + SERVICE_PURGEKEYS + "> which is part of the key to match";
      } else {
        String purgedInfo = purgeKeys(Parameter.trim());
        return purgedInfo;
      }
    } else {
      // We didn't deal with it, does our parent?
      return super.processControlEvent(Command, Init, Parameter);
    }
  }
}
