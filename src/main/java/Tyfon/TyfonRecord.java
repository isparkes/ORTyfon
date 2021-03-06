/* ====================================================================
 * Limited Evaluation License:
 *
 * The exclusive owner of this work is Tiger Shore Management Ltd.
 * This work, including all associated documents and components
 * is Copyright Tiger Shore Management Limited 2006-2010.
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
 *
 * This software is provided "as is" and any expressed or impled warranties,
 * including, but not limited to, the impled warranties of merchantability
 * and fitness for a particular purpose are disclaimed. In no event shall
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
package Tyfon;

import OpenRate.record.ErrorType;
import OpenRate.record.RatingRecord;
import OpenRate.record.RecordError;
import fraud.FraudAlert;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * A Record corresponds to a unit of work that is being processed by the
 * pipeline. Records are created in the InputAdapter, pass through the Pipeline,
 * and written out in the OutputAdapter. Any stage of the pipeline my update the
 * record in any way, provided that later stages in the processing and the
 * output adapter know how to treat the record they receive.
 *
 * As an alternative, you may define a less flexible record format as you wish
 *
 * and fill in the fields as required, but this costs performance.
 *
 * Generally, the record should know how to handle the following operations by
 * linking the appropriate method:
 *
 * mapOriginalData() [mandatory] ----------------- Transformation from a flat
 * record as read by the input adapter to a formatted record.
 *
 * unmapOriginalData() [mandatory if you wish to write output files]
 * ------------------- Transformation from a formatted record to a flat record
 * ready for output.
 *
 * getDumpInfo() [optional] ------------- Preparation of the dump equivalent of
 * the formatted record, ready for dumping out to a dump file.
 *
 * In this simple example, we require only to read the "B-Number", and write the
 * "Destination" as a result of this. Because of the simplicity of the example
 * we do not perform a full mapping, we just handle the fields we want directly,
 * which is one of the advantages of the BBPA model (map as much as you want or
 * as little as you have to).
 *
 */
public class TyfonRecord extends RatingRecord {
  // **************************** Ventelo Definition ***************************

  // Character used as a field splitter
  private static final String VENTELO_FIELD_SPLITTER = ";";

  // Input record types
  public final static String VENTELO_TYPE_HEADER = "100";
  public final static String VENTELO_TYPE_DETAIL = "300";
  public final static String VENTELO_TYPE_TRAILER = "900";

  // processing record types
  public final static int VENTELO_HEADER_RECORD = 10;
  public final static int VENTELO_DETAIL_RECORD = 20;
  public final static int VENTELO_TRAILER_RECORD = 90;

  // Header
  private final static int FIELD_H_SEQUENCE_NUMBER = 1;
  private final static int FIELD_H_CREATION_TIMESTAMP = 2;
  private final static int FIELD_H_TRANSMISSION_DATE = 3;
  private final static int FIELD_H_SPECIFICATION_VERSION_NUMBER = 4;

  // Detail Records
  private final static int FIELD_V_COUNT = 13;
  private final static int FIELD_V_RECORD_NUMBER = 1;
  private final static int FIELD_V_A_NUMBER = 2;
  private final static int FIELD_V_B_NUMBER = 3;
  private final static int FIELD_V_CHARGING_START_DATE = 4;
  private final static int FIELD_V_CHARGING_START_TIME = 5;
  private final static int FIELD_V_DURATION = 6;
  private final static int FIELD_V_CHARGE = 7;
  private final static int FIELD_V_INTER_OPERATOR_ID = 9;
  private final static int FIELD_V_TRAFFICCASEID = 12;

  // Trailer
  private final static int FIELD_T_TOTAL_NUMBER_RECORDS = 1;

  // **************************** Telavox Definition ***************************
  // Character used as a field splitter
  private static final String TELAVOX_FIELD_SPLITTER = ";";

  // Input record prefixes
  public final static String TELAVOX_TYPE_HEADER = "100";
  public final static String TELAVOX_TYPE_DETAIL = "300";
  public final static String TELAVOX_TYPE_TRAILER = "900";

  // processing record types
  public final static int TELAVOX_DETAIL_RECORD = 21;

  // Detail Records
  private final static int FIELD_T_A_NUMBER = 0;
  private final static int FIELD_T_B_NUMBER = 1;
  private final static int FIELD_T_CALL_DATE = 2;
  private final static int FIELD_T_CALL_TIME = 3;
  private final static int FIELD_T_DURATION = 4;
  private final static int FIELD_T_CONN_COST_RETAIL = 5;
  private final static int FIELD_T_TOTAL_COST_RETAIL = 6;
  private final static int FIELD_T_CONN_COST_ORIG = 7;
  private final static int FIELD_T_TOTAL_COST_ORIG = 8;

  // **************************** Fraud Definition ***************************
  // Character used as a field splitter
  private static final String FRAUD_FIELD_SPLITTER = "\",\"";

  // Records to be processed
  public final static int FRAUD_DETAIL_RECORD = 22;

  // The number of fields we expect
  private final static int FRAUD_FIELD_COUNT = 12;

  // Detail Records
  public static final int FIELD_F_START_TIME = 0;
  private final static int FIELD_F_END_TIME = 1;
  private final static int FIELD_F_CALL_DUR = 2;
  private final static int FIELD_F_BILL_DUR = 3;
  private final static int FIELD_F_A_NUMBER = 4;
  private final static int FIELD_F_B_NUMBER = 5;
  private final static int FIELD_F_A_NUMBER_CLID = 6;
  private final static int FIELD_F_FORWARD = 7;
  private final static int FIELD_F_DIRECTION = 8;
  private final static int FIELD_F_SIP_PEER = 9;
  private final static int FIELD_F_RESULT = 10;
  private final static int FIELD_F_DISPOSITION = 11;

  // **************************** Bahnhof Definition ***************************
  // Character used as a field splitter
  public static final String BAHNHOF_FIELD_SPLITTER = ",";

  // Used to identify the header record in the file
  public final static String BAHNOF_TYPE_HEADER_PREFIX = ".*start_at.*";

  // processing record types
  public final static int BAHNHOF_DETAIL_RECORD = 23;

  // Detail Records - bahnhof CDR format
  public final static int FIELD_B_COUNT = 14;
  public final static int FIELD_B_start_at = 0;
  public final static int FIELD_B_cdr_source = 1;
  public final static int FIELD_B_upstream_id = 2;
  public final static int FIELD_B_caller = 3;
  public final static int FIELD_B_callee = 4;
  public final static int FIELD_B_duration = 5;
  public final static int FIELD_B_dest_id = 6;
  public final static int FIELD_B_category_id = 7;
  public final static int FIELD_B_child_key = 8;
  public final static int FIELD_B_amount = 9;
  public final static int FIELD_B_vat_rate = 10;
  public final static int FIELD_B_confidential = 11;
  public final static int FIELD_B_description = 12;
  public final static int FIELD_B_outgoing = 13;

  //  The record type is what allows us to determine what the records to handle
  //	are, and what to ignore. Generally you will need something of this type
  public static final String RECYCLE_TAG = "ORRECYCLE";

  private static final long serialVersionUID = 732626781L;

  // CDR related variables
  public String callDate = null; // Date of the call
  public int callTime;        // Duration of the call
  public String A_Number;         // Raw A Number
  public String B_Number;         // Raw B Number
  public String B_NumberNorm;     // Normalised B number
  public String supplier = null;  // The supplier of the call record
  public String direction = "";   // call direction, Originating or Terminating

  // Rating variables
  public String Destination;      // The zoning destination for the B Number
  public String Zone_Cat;         // The category for the B Number
  public String Dest_Phone_Type;  // The type of number
  public String TimeResult;         // The time zone
  //public String RetailPriceGroup; // The retail price group
  public Boolean isPremium = false;

  // Output rated amount values
  public double origAmount = 0;
  public double outputTotalCost = 0;
  public double outputConnCost = 0;

  // Internal Management Fields
  public Integer CustIDA = null;    // The identifier of the A customer
  public String subscriptionID;    // Subscription ID used for posting in billing
  public String UsedProduct;       // The identifier of the product
  public String baseProduct;       // The base price plan
  public ArrayList<String> overlay; // Overlay price plan(s)
  public String markupType;        // The type of markup, "" for none
  public boolean isMarkup;          // Quck flag for checking if we are doing markup
  public String marginFlag;        // Used for reporting incorrect margins

  // The number of recycles for this record
  public int recycleCount = 0;

  // If there was one or more fraud notifications, we note it here
  public ArrayList<FraudAlert> alerts = new ArrayList<>();

  /**
   * Utility function to map a file header record
   *
   * @param inputData The input data to map
   */
  public void mapVenteloHeaderRecord(String inputData) {
    RECORD_TYPE = TyfonRecord.VENTELO_HEADER_RECORD;
    OriginalData = inputData;
  }

  /**
   * Utility function to map a file trailer record
   *
   * @param inputData The input data to map
   */
  public void mapVenteloTrailerRecord(String inputData) {
    RECORD_TYPE = TyfonRecord.VENTELO_TRAILER_RECORD;
    OriginalData = inputData;
  }

  /**
   * Map a detail record from the Ventelo input source. We split up the record
   * at the tabs, and put the information into fields so that we can manipulate
   * it as we want.
   *
   * @param inputData The input data to map
   */
  public void mapVenteloDetailRecord(String inputData) {
    // Set the record type
    RECORD_TYPE = TyfonRecord.VENTELO_DETAIL_RECORD;

    // Set the original data
    OriginalData = inputData;

    // Detect recycle case
    if (OriginalData.startsWith(RECYCLE_TAG)) {
      // RECYCLE_COUNT
      StringBuffer record = new StringBuffer(OriginalData);

      // remove RecycleTag from record
      record = record.delete(0, record.indexOf(VENTELO_FIELD_SPLITTER) + 1);

      // remove ErrorCode from record
      record = record.delete(0, record.indexOf(VENTELO_FIELD_SPLITTER) + 1);

      // Get the previous recycle count
      String Recycle_CountStr = record.substring(0, record.indexOf(VENTELO_FIELD_SPLITTER));
      recycleCount = Integer.parseInt(Recycle_CountStr);

      // remove RecycleCount from record
      record = record.delete(0, record.indexOf(";") + 1);

      // reset the original data
      OriginalData = record.toString();
    }

    // Split the fields up
    fields = OriginalData.split(VENTELO_FIELD_SPLITTER);

    // Validate the number of fields
    if (fields.length == FIELD_V_COUNT) {
      callDate = getField(FIELD_V_CHARGING_START_DATE) + getField(FIELD_V_CHARGING_START_TIME);
      direction = "Originating";
      A_Number = getField(FIELD_V_A_NUMBER);
      B_Number = getField(FIELD_V_B_NUMBER);

      try {
        callTime = Integer.parseInt(getField(FIELD_V_DURATION));
      } catch (NumberFormatException nfe) {
        addError(new RecordError("ERR_DURATION_INVALID", ErrorType.DATA_VALIDATION));
      }

      try {
        origAmount = Double.parseDouble(getField(FIELD_V_CHARGE));
      } catch (NumberFormatException nfe) {
        addError(new RecordError("ERR_ORIG_PRICE_INVALID", ErrorType.DATA_VALIDATION));
      }

      // Get the supplier string for markup processing
      supplier = getField(FIELD_V_INTER_OPERATOR_ID);

      //Repair full prefix for National calls
      if (B_Number.length() > 5 && !B_Number.substring(1, 2).matches("0") && B_Number.substring(0, 1).matches("0")) {
        B_NumberNorm = "0046" + B_Number.substring(1, B_Number.length());
      } //Repair 118118 and others
      else if (B_Number.length() < 7 && !B_Number.substring(1, 2).matches("0") && B_Number.substring(0, 2).matches("11")) {
        B_NumberNorm = "0046" + B_Number;
      } // International
      else if (B_Number.startsWith("00")) {
        // Do nothing it is already right
        B_NumberNorm = B_Number;
      } // Default error case
      else {
        addError(new RecordError("ERR_NORM_FAILED", ErrorType.DATA_VALIDATION));
      }

      // Get the CDR date
      try {
        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyyMMddHHmmss");
        EventStartDate = sdfInput.parse(callDate);
        UTCEventDate = EventStartDate.getTime() / 1000;
      } catch (ParseException ex) {
        addError(new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION));
      }

      // Set the RUMS duration and original rated amount (for markup)
      setRUMValue("DUR", callTime);
      setRUMValue("SEK", origAmount);

      // Set the default service
      Service = "TEL";
    } else {
      addError(new RecordError("ERR_FIELD_COUNT", ErrorType.DATA_VALIDATION));
    }
  }

  /**
   * Map a detail record from the Bahnhof input source. We split up the record
   * at the tabs, and put the information into fields so that we can manipulate
   * it as we want.
   *
   * @param inputData The input data to map
   */
  public void mapBahnhofDetailRecord(String inputData) {
    // Set the record type
    RECORD_TYPE = TyfonRecord.BAHNHOF_DETAIL_RECORD;

    // Set the original data
    OriginalData = inputData;

    // Detect recycle case
    if (OriginalData.startsWith(RECYCLE_TAG)) {
      // RECYCLE_COUNT
      StringBuffer record = new StringBuffer(OriginalData);

      // remove RecycleTag from record
      record = record.delete(0, record.indexOf(VENTELO_FIELD_SPLITTER) + 1);

      // remove ErrorCode from record
      record = record.delete(0, record.indexOf(VENTELO_FIELD_SPLITTER) + 1);

      // Get the previous recycle count
      String Recycle_CountStr = record.substring(0, record.indexOf(VENTELO_FIELD_SPLITTER));
      recycleCount = Integer.parseInt(Recycle_CountStr);

      // remove RecycleCount from record
      record = record.delete(0, record.indexOf(VENTELO_FIELD_SPLITTER) + 1);

      // reset the original data
      OriginalData = record.toString();
    }

    // Split the fields up
    fields = OriginalData.split(BAHNHOF_FIELD_SPLITTER);

    // Validate the number of fields
    if (fields.length == FIELD_B_COUNT) {
      callDate = getField(FIELD_B_start_at);

      if (getField(FIELD_B_outgoing).equals("1")) {
        direction = "Originating";
        A_Number = getField(FIELD_B_caller).replaceAll("\"", "");
        B_Number = getField(FIELD_B_callee).replaceAll("\"", "");
      } else {
        direction = "Terminating";
        A_Number = getField(FIELD_B_callee).replaceAll("\"", "");
        B_Number = getField(FIELD_B_caller).replaceAll("\"", "");
      }

      try {
        callTime = Integer.parseInt(getField(FIELD_B_duration));
      } catch (NumberFormatException nfe) {
        addError(new RecordError("ERR_DURATION_INVALID", ErrorType.DATA_VALIDATION));
      }

      try {
        origAmount = Double.parseDouble(getField(FIELD_B_amount));
      } catch (NumberFormatException nfe) {
        addError(new RecordError("ERR_ORIG_PRICE_INVALID", ErrorType.DATA_VALIDATION));
      }

      // Get the supplier string for markup processing
      supplier = getField(FIELD_B_cdr_source);

      // A Number normalisation
      if (A_Number.startsWith("46")) {
        // replace the "46" prefix with "0"
        A_Number = A_Number.replaceAll("^46", "0");
      } else {
        addError(new RecordError("ERR_A_NORM", ErrorType.DATA_VALIDATION));
      }

      // B Number normalisation
      if (direction.equals("Originating")) {
        if (B_Number.startsWith("00")) {
          // Do nothing - it is already right
          B_NumberNorm = B_Number;
        } //Repair 118118 and others
        else if (B_Number.length() < 7 && !B_Number.substring(1, 2).matches("0") && B_Number.substring(0, 2).matches("11")) {
          B_NumberNorm = "0046" + B_Number;
        } // All other
        else {
          B_NumberNorm = "00" + B_Number;
        } // Default error case
      } else {
        B_Number = "";
        B_NumberNorm = "";
      }

      // Get the CDR date
      try {
        DateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdfInput.setTimeZone(TimeZone.getTimeZone("Zulu"));
        EventStartDate = sdfInput.parse(callDate);
        DateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdfOutput.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
        callDate = sdfOutput.format(EventStartDate);
        UTCEventDate = EventStartDate.getTime() / 1000;
      } catch (ParseException ex) {
        addError(new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION));
      }

      if (direction.equals("Terminating")) {
        addError(new RecordError("ERR_TERMINATING", ErrorType.DATA_VALIDATION));
      }

      // Set the RUMS duration and original rated amount (for markup)
      setRUMValue("DUR", callTime);
      setRUMValue("SEK", origAmount);

      // Set the default service
      Service = "TEL";
    } else {
      addError(new RecordError("ERR_FIELD_COUNT", ErrorType.DATA_VALIDATION));
    }
  }

  /**
   * Map a detail record from the Telavox input source. We split up the record
   * at the tabs, and put the information into fields so that we can manipulate
   * it as we want.
   *
   * @param inputData The input data to map
   */
  void mapTelavoxRecord(String inputData) {
    // Set the record type
    RECORD_TYPE = TyfonRecord.TELAVOX_DETAIL_RECORD;

    // Set the original data
    OriginalData = inputData;

    // Detect recycle case
    if (OriginalData.startsWith(RECYCLE_TAG)) {
      // RECYCLE_COUNT
      StringBuffer record = new StringBuffer(OriginalData);

      // remove RecycleTag from record
      record = record.delete(0, record.indexOf(TELAVOX_FIELD_SPLITTER) + 1);

      // remove ErrorCode from record
      record = record.delete(0, record.indexOf(TELAVOX_FIELD_SPLITTER) + 1);

      // Get the previous recycle count
      String Recycle_CountStr = record.substring(0, record.indexOf(TELAVOX_FIELD_SPLITTER));
      recycleCount = Integer.parseInt(Recycle_CountStr);

      // remove RecycleCount from record
      record = record.delete(0, record.indexOf(";") + 1);

      // reset the original data
      OriginalData = record.toString();
    }

    // Split the fields up
    fields = OriginalData.split(TELAVOX_FIELD_SPLITTER);

    callDate = getField(FIELD_T_CALL_DATE) + getField(FIELD_T_CALL_TIME);
    A_Number = getField(FIELD_T_A_NUMBER);
    B_Number = getField(FIELD_T_B_NUMBER);

    try {
      callTime = Integer.parseInt(getField(FIELD_T_DURATION));
    } catch (NumberFormatException nfe) {
      addError(new RecordError("ERR_DURATION_INVALID", ErrorType.DATA_VALIDATION));
    }

    try {
      origAmount = Double.parseDouble(getField(FIELD_T_TOTAL_COST_ORIG).replaceAll(",", "."));
    } catch (NumberFormatException nfe) {
      addError(new RecordError("ERR_ORIG_PRICE_INVALID", ErrorType.DATA_VALIDATION));
    }

    supplier = "Telavox";

    //Repair full prefix for National calls
    if (B_Number.length() > 5 && !B_Number.substring(1, 2).matches("0") && B_Number.substring(0, 1).matches("0")) {
      B_NumberNorm = "0046" + B_Number.substring(1, B_Number.length());
    } //Repair 118118 and others
    else if (B_Number.length() < 7 && !B_Number.substring(1, 2).matches("0") && B_Number.substring(0, 2).matches("11")) {
      B_NumberNorm = "0046" + B_Number;
    } // International
    else if (B_Number.startsWith("00")) {
      // Do nothing it is already right
      B_NumberNorm = B_Number;
    } // Default error case
    else {
      addError(new RecordError("ERR_NORM_FAILED", ErrorType.DATA_VALIDATION));
    }

    // Get the CDR date
    try {
      SimpleDateFormat sdfInput = new SimpleDateFormat("yyyyMMddHHmmss");
      EventStartDate = sdfInput.parse(callDate);
      UTCEventDate = EventStartDate.getTime() / 1000;
    } catch (ParseException ex) {
      addError(new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION));
    }

    // Set the RUMS duration and original rated amount (for markup)
    setRUMValue("DUR", callTime);
    setRUMValue("SEK", origAmount);

    // Set the default service
    Service = "TEL";
  }

  /**
   * Utility function to map an unknown type of record
   *
   * @param inputData The input data to map
   */
  public void mapUnknownRecord(String inputData) {
    // Just mark the record as unknown and store the input
    OriginalData = inputData;
    addError(new RecordError("ERR_UNKNOWN_RECORD", ErrorType.DATA_VALIDATION));
  }

  /**
   * Reconstruct the record from the field values
   *
   * @return The unmapped original data
   */
  public String unmapOriginalData() {
    int NumberOfFields;
    int i;
    StringBuffer tmpReassemble;

    if (RECORD_TYPE == TyfonRecord.VENTELO_DETAIL_RECORD) {
      // We use the string buffer for the reassembly of the record. Avoid
      // just catenating strings, as it is a LOT slower because of the
      // java internal string handling (it has to allocate/deallocate many
      // times to rebuild the string).
      tmpReassemble = new StringBuffer(1024);

      // write the destination information back
      // setField(DESTINATION_IDX, Destination);
      NumberOfFields = fields.length;

      for (i = 0; i < NumberOfFields; i++) {

        if (i == 0) {
          tmpReassemble.append(fields[i]);
        } else {
          tmpReassemble.append(TELAVOX_FIELD_SPLITTER);
          tmpReassemble.append(fields[i]);
        }
      }

      return tmpReassemble.toString();
    } else {
      // just return the untampered with original
      return OriginalData;
    }
  }

  /**
   * Reconstruct the record from the field values, prefixing it with the Recycle
   * tag
   *
   * @return The unmapped data
   */
  public String unmapVenteloSuspenseData() {
    StringBuffer tmpReassemble;

    if (RECORD_TYPE == TyfonRecord.VENTELO_DETAIL_RECORD) {
      // We use the string buffer for the reassembly of the record. Avoid
      // just catenating strings, as it is a LOT slower because of the
      // java internal string handling (it has to allocate/deallocate many
      // times to rebuild the string).
      tmpReassemble = new StringBuffer(1024);

      // Write the error information back, including the recycle header
      String errorCode = this.getErrors().get(0).getMessage();

      // Increment the recycle count
      recycleCount++;

      // Put the header on
      tmpReassemble.append(RECYCLE_TAG);
      tmpReassemble.append(VENTELO_FIELD_SPLITTER);
      tmpReassemble.append(errorCode);
      tmpReassemble.append(VENTELO_FIELD_SPLITTER);
      tmpReassemble.append(recycleCount);
      tmpReassemble.append(VENTELO_FIELD_SPLITTER);

      // Now the original record
      tmpReassemble.append(OriginalData);

      return tmpReassemble.toString();
    } else {
      // just return the untampered with original
      return OriginalData;
    }
  }

  /**
   * Reconstruct the record from the field values, prefixing it with the Recycle
   * tag
   *
   * @return The unmapped data
   */
  String unmapTelavoxSuspenseData() {
    StringBuffer tmpReassemble;

    if (RECORD_TYPE == TyfonRecord.TELAVOX_DETAIL_RECORD) {
      // We use the string buffer for the reassembly of the record. Avoid
      // just catenating strings, as it is a LOT slower because of the
      // java internal string handling (it has to allocate/deallocate many
      // times to rebuild the string).
      tmpReassemble = new StringBuffer(1024);

      // Write the error information back, including the recycle header
      String errorCode = this.getErrors().get(0).getMessage();

      // Increment the recycle count
      recycleCount++;

      // Put the header on
      tmpReassemble.append(RECYCLE_TAG);
      tmpReassemble.append(TELAVOX_FIELD_SPLITTER);
      tmpReassemble.append(errorCode);
      tmpReassemble.append(TELAVOX_FIELD_SPLITTER);
      tmpReassemble.append(recycleCount);
      tmpReassemble.append(TELAVOX_FIELD_SPLITTER);

      // Now the original record
      tmpReassemble.append(OriginalData);

      return tmpReassemble.toString();
    } else {
      // just return the untampered with original
      return OriginalData;
    }
  }

  /**
   * Reconstruct the record from the field values, prefixing it with the Recycle
   * tag
   *
   * @return The unmapped data
   */
  String unmapBahnhofSuspenseData() {
    StringBuffer tmpReassemble;

    if (RECORD_TYPE == TyfonRecord.BAHNHOF_DETAIL_RECORD) {
      // We use the string buffer for the reassembly of the record. Avoid
      // just catenating strings, as it is a LOT slower because of the
      // java internal string handling (it has to allocate/deallocate many
      // times to rebuild the string).
      tmpReassemble = new StringBuffer(1024);

      // Write the error information back, including the recycle header
      String errorCode = this.getErrors().get(0).getMessage();

      // Increment the recycle count
      recycleCount++;

      // Put the header on
      tmpReassemble.append(RECYCLE_TAG);
      tmpReassemble.append(VENTELO_FIELD_SPLITTER);
      tmpReassemble.append(errorCode);
      tmpReassemble.append(VENTELO_FIELD_SPLITTER);
      tmpReassemble.append(recycleCount);
      tmpReassemble.append(VENTELO_FIELD_SPLITTER);

      // Now the original record
      tmpReassemble.append(OriginalData);

      return tmpReassemble.toString();
    } else {
      // just return the untampered with original
      return OriginalData;
    }
  }

  /**
   * Map a record for fraud detection
   *
   * @param inputData
   */
  public void mapFraudDetailRecord(String inputData) {
    // Set the record type
    RECORD_TYPE = TyfonRecord.FRAUD_DETAIL_RECORD;

    // Set the original data
    OriginalData = inputData;

    // Split the fields up
    fields = OriginalData.split(FRAUD_FIELD_SPLITTER);
    if (fields.length == FRAUD_FIELD_COUNT) {
      // remove the hanging separators
      setField(FIELD_F_START_TIME, getField(FIELD_F_START_TIME).replaceAll("\"", ""));
      setField(FIELD_F_DISPOSITION, getField(FIELD_F_DISPOSITION).replaceAll("\"", ""));

      callDate = getField(FIELD_F_START_TIME).replaceAll("\"", "");
      A_Number = getField(FIELD_F_A_NUMBER);
      B_Number = getField(FIELD_F_B_NUMBER);

      try {
        // Using the call duration rather than the billable duration
        callTime = Integer.parseInt(getField(FIELD_F_CALL_DUR));
      } catch (NumberFormatException nfe) {
        addError(new RecordError("ERR_DURATION_INVALID", ErrorType.DATA_VALIDATION));
      }

      supplier = "Internal";

      if (B_Number.isEmpty()) {
        addError(new RecordError("ERR_NORM_FAILED", ErrorType.DATA_VALIDATION));
      } else {
        //Repair full prefix for National calls
        if (B_Number.length() > 5 && !B_Number.substring(1, 2).matches("0") && B_Number.substring(0, 1).matches("0")) {
          B_NumberNorm = "0046" + B_Number.substring(1, B_Number.length());
        } //Repair 118118 and others
        else if (B_Number.length() < 7 && !B_Number.substring(1, 2).matches("0") && B_Number.substring(0, 2).matches("11")) {
          B_NumberNorm = "0046" + B_Number;
        } // International
        else if (B_Number.startsWith("00")) {
          // Do nothing it is already right
          B_NumberNorm = B_Number;
        } // Default error case
        else {
          addError(new RecordError("ERR_NORM_FAILED", ErrorType.DATA_VALIDATION));
        }
      }

      // Get the CDR date
      try {
        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        EventStartDate = sdfInput.parse(callDate);
        UTCEventDate = EventStartDate.getTime() / 1000;
        SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyyMMddHHmmss");

        // Prepare the short date for Fraud
        callDate = sdfOutput.format(EventStartDate);
      } catch (ParseException ex) {
        addError(new RecordError("ERR_DATE_INVALID", ErrorType.DATA_VALIDATION));
      }

      // Filter out the records we are not interested in
      if (getField(FIELD_F_DIRECTION).equalsIgnoreCase("inbound")) {
        addError(new RecordError("DISC_DIRECTION", ErrorType.DATA_VALIDATION));
      }

      // Filter out the records we are not interested in
      if (getField(FIELD_F_DISPOSITION).equalsIgnoreCase("answered") == false) {
        addError(new RecordError("DISC_ANSWER", ErrorType.DATA_VALIDATION));
      }

      // Set the default service
      Service = "TEL";
    } else {
      addError(new RecordError("ERR_FIELD_COUNT", ErrorType.DATA_VALIDATION));
    }
  }

  /**
   * Return the dump-ready data
   *
   * @return The dump info strings
   */
  @Override
  public ArrayList<String> getDumpInfo() {

    ArrayList<String> tmpDumpList;
    tmpDumpList = new ArrayList<>();

    // Format the fields
    // We only transform the detail records, and leave the others alone
    if ((RECORD_TYPE == TyfonRecord.VENTELO_DETAIL_RECORD)
            || (RECORD_TYPE == TyfonRecord.TELAVOX_DETAIL_RECORD)
            || (RECORD_TYPE == TyfonRecord.BAHNHOF_DETAIL_RECORD)) {
      tmpDumpList.add("============ BEGIN RECORD ============");
      tmpDumpList.add("  Record Number         = <" + RecordNumber + ">");
      tmpDumpList.add("  Outputs               = <" + outputs + ">");
      tmpDumpList.add("  Original data         = <" + OriginalData + ">");
      tmpDumpList.add("--------------------------------------");
      tmpDumpList.add("  Call_Date             = <" + callDate + ">");
      tmpDumpList.add("  Call_Time             = <" + callTime + ">");
      tmpDumpList.add("  A_Number              = <" + A_Number + ">");
      tmpDumpList.add("  B_Number              = <" + B_Number + ">");
      tmpDumpList.add("  OrigRatedAmount       = <" + origAmount + ">");
      tmpDumpList.add("  Direction             = <" + direction + ">");
      tmpDumpList.add("--------------------------------------");
      tmpDumpList.add("  CustIDA               = <" + CustIDA + ">");
      tmpDumpList.add("  Subscription ID       = <" + subscriptionID + ">");
      tmpDumpList.add("  CDRDate               = <" + EventStartDate + ">");
      tmpDumpList.add("  B_Number Norm         = <" + B_NumberNorm + ">");
      tmpDumpList.add("  Destination           = <" + Destination + ">");
      tmpDumpList.add("  Zone_Cat              = <" + Zone_Cat + ">");
      tmpDumpList.add("  Dest_Phone_Type       = <" + Dest_Phone_Type + ">");
      tmpDumpList.add("  TimeZone              = <" + TimeResult + ">");
      tmpDumpList.add("  Supplier              = <" + supplier + ">");
      tmpDumpList.add("  Markup                = <" + isMarkup + ">");
      tmpDumpList.add("  Markup Type           = <" + markupType + ">");
      tmpDumpList.add("  Premium               = <" + isPremium + ">");
      tmpDumpList.add("--------------------------------------");
      tmpDumpList.add("  UsedProduct           = <" + UsedProduct + ">");
      tmpDumpList.add("  Base Product          = <" + baseProduct + ">");
      tmpDumpList.add("  Overlay Product       = <" + overlay + ">");
      tmpDumpList.add("  RatedAmount           = <" + outputTotalCost + ">");
      tmpDumpList.add("  RatedAmountConnFee    = <" + outputConnCost + ">");

      // Charge Packets
      tmpDumpList.addAll(getChargePacketsDump());

      // Errors
      tmpDumpList.addAll(getErrorDump());
    } else if (RECORD_TYPE == TyfonRecord.FRAUD_DETAIL_RECORD) {
      tmpDumpList.add("============ FRAUD RECORD ============");
      tmpDumpList.add("  Record Number         = <" + RecordNumber + ">");
      tmpDumpList.add("  Outputs               = <" + outputs + ">");
      tmpDumpList.add("  Original data         = <" + OriginalData + ">");
      tmpDumpList.add("--------------------------------------");
      tmpDumpList.add("  START_TIME            = <" + getField(FIELD_F_START_TIME) + ">");
      tmpDumpList.add("  END_TIME              = <" + getField(FIELD_F_END_TIME) + ">");
      tmpDumpList.add("  CALL_DUR              = <" + getField(FIELD_F_CALL_DUR) + ">");
      tmpDumpList.add("  BILL_DUR              = <" + getField(FIELD_F_BILL_DUR) + ">");
      tmpDumpList.add("  A_NUMBER              = <" + getField(FIELD_F_A_NUMBER) + ">");
      tmpDumpList.add("  B_NUMBER              = <" + getField(FIELD_F_B_NUMBER) + ">");
      tmpDumpList.add("  A_NUMBER_CLID         = <" + getField(FIELD_F_A_NUMBER_CLID) + ">");
      tmpDumpList.add("  FORWARD               = <" + getField(FIELD_F_FORWARD) + ">");
      tmpDumpList.add("  DIRECTION             = <" + getField(FIELD_F_DIRECTION) + ">");
      tmpDumpList.add("  SIP_PEER              = <" + getField(FIELD_F_SIP_PEER) + ">");
      tmpDumpList.add("  RESULT                = <" + getField(FIELD_F_RESULT) + ">");
      tmpDumpList.add("  DISPOSITION           = <" + getField(FIELD_F_DISPOSITION) + ">");
      tmpDumpList.add("--------------------------------------");
      tmpDumpList.add("  Call_Date             = <" + callDate + ">");
      tmpDumpList.add("  Call_Time             = <" + callTime + ">");
      tmpDumpList.add("  A_Number              = <" + A_Number + ">");
      tmpDumpList.add("  B_Number              = <" + B_Number + ">");
      tmpDumpList.add("--------------------------------------");
      tmpDumpList.add("  CustIDA               = <" + CustIDA + ">");
      tmpDumpList.add("  Subscription ID       = <" + subscriptionID + ">");
      tmpDumpList.add("  CDRDate               = <" + EventStartDate + ">");
      tmpDumpList.add("  B_Number Norm         = <" + B_NumberNorm + ">");
      tmpDumpList.add("  Destination           = <" + Destination + ">");
      tmpDumpList.add("  Zone_Cat              = <" + Zone_Cat + ">");
      tmpDumpList.add("  Dest_Phone_Type       = <" + Dest_Phone_Type + ">");
      tmpDumpList.add("  Markup                = <" + isMarkup + ">");
      tmpDumpList.add("  Premium               = <" + isPremium + ">");
      tmpDumpList.add("--------------------------------------");
      tmpDumpList.add("  Alerts                = <" + alerts.size() + ">");

      for (FraudAlert alert : alerts) {
        tmpDumpList.add("    Name                = <" + alert.getThresholdName() + ">");
        tmpDumpList.add("    Limit               = <" + alert.getNotificationLimit() + ">");
        tmpDumpList.add("    Balance             = <" + alert.getBalance() + ">");
        tmpDumpList.add("    ----------------------------------");
      }

      // Charge Packets
      tmpDumpList.addAll(getChargePacketsDump());

      // Errors
      tmpDumpList.addAll(getErrorDump());
    }

    return tmpDumpList;
  }

  public Object getSourceKey() {
    return null;
  }
}
