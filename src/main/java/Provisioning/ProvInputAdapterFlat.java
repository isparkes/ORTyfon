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
package Provisioning;

import OpenRate.adapter.file.FlatFileInputAdapter;
import OpenRate.exception.InitializationException;
import OpenRate.record.*;
import OpenRate.utils.ConversionUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.util.Arrays;

/**
 * OSS (Operational Support System) input adapter
 */
public class ProvInputAdapterFlat extends FlatFileInputAdapter {

  // This is the stream record number counter which tells us the number of
  // the compressed records

  private int StreamRecordNumber;

  // This is the object that is used to compress the records
  String custId = null;
  String serviceId = null;
  String number = null;
  String pricePlan = null;
  String validFrom = null;
  String validTo = null;
  String validFromSegment = null;
  String validToSegment = null;
  boolean inSegment = false;      // Used to get segment dates
  boolean inTariff = false;       // Used to get tariff dates

  // get the conversion object
  private ConversionUtils conv;

  /**
   * The record identifier is used as the separator between records. It is not
   * possible/efficient to perform parsing on very long xml streams, we
   * therefore use traditional flat file based techniques to separate the
   * records out of the stream, before passing each individual record for
   * parsing.
   */
  protected String TYFON_RECORD_IDENTIFIER = "customer";

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public void init(String PipelineName, String ModuleName)
          throws InitializationException {
    // perform te core preparation first
    super.init(PipelineName, ModuleName);

    // Prepare the date conversion object
    conv = ConversionUtils.getConversionCache().getConversionObject(getSymbolicName());
    conv.setInputDateFormat("yyyy-MM-dd hh:mm:ss");
  }

  @Override
  public IRecord procHeader(IRecord r) {
    // reset the record numbering
    StreamRecordNumber = 0;

    return r;
  }

  @Override
  public IRecord procValidRecord(IRecord r) {
    ProvRecord outputRecord = null;
    FlatRecord inputRecord = (FlatRecord) r;

    String tmpData = inputRecord.getData().trim();

    // if we have got the customer, then store this
    if (tmpData.startsWith("<customer ")) {
      // We got the customer tag - get the data
      custId = tmpData.replaceAll("<customer customerId=", "").replaceAll(">", "").replaceAll("\"", "");
    } // Added as new service id
    else if (tmpData.startsWith("<account ")) {
      serviceId = tmpData.replaceAll("<account serviceId=", "").replaceAll(">", "").replaceAll("\"", "").trim();
      inSegment = true;
    } else if (tmpData.startsWith("<number>")) {
      number = tmpData.replaceAll("<number>", "").replaceAll("</number>", "").trim();
    } // Appears to be deprecated
    else if (tmpData.startsWith("<pricePlan>")) {
      inSegment = false;
      inTariff = true;
    } // New version of "priceplan"
    else if (tmpData.startsWith("<code>")) {
      pricePlan = tmpData.replaceAll("<code>", "").replaceAll("</code>", "");
    } else if (tmpData.startsWith("<validFrom>")) {
      if (inTariff) {
        validFrom = tmpData.replaceAll("<validFrom>", "").replaceAll("</validFrom>", "");
      }
      if (inSegment) {
        validFromSegment = tmpData.replaceAll("<validFrom>", "").replaceAll("</validFrom>", "");
      }
    } else if (tmpData.startsWith("<validTo>")) {
      if (inTariff) {
        validTo = tmpData.replaceAll("<validTo>", "").replaceAll("</validTo>", "");
      }
      if (inSegment) {
        validToSegment = tmpData.replaceAll("<validTo>", "").replaceAll("</validTo>", "");
      }
    } // output the record if we have one to output, keep the customer data
    else if (tmpData.startsWith("</pricePlan>")) {
      inTariff = false;

      // Create the record and pump it out
      outputRecord = new ProvRecord();
      outputRecord.setCustId(custId);
      outputRecord.setServiceId(serviceId);
      outputRecord.setNumber(number);
      outputRecord.setPricePlan(pricePlan);
      outputRecord.setValidFrom(validFrom);
      outputRecord.setValidTo(validTo);
      outputRecord.setSegmentValidFrom(validFromSegment);
      outputRecord.setSegmentValidTo(validToSegment);

      // deal with empty dates
      if (validFrom.isEmpty()) {
        validFrom = "1970-01-01";
      }

      // deal with empty dates
      if (validTo.isEmpty()) {
        validTo = "2036-12-31";
      }

      // deal with empty dates
      if (validFromSegment.isEmpty()) {
        validFromSegment = "1970-01-01";
      }

      // deal with empty dates
      if (validToSegment.isEmpty()) {
        validToSegment = "2036-12-31";
      }

      try {
        outputRecord.setValidFromUTC(conv.convertInputDateToUTC(validFrom + " 00:00:00"));
      } catch (ParseException ex) {
        // we could not parse the subscriptionID to be an integer
        RecordError tmpError = new RecordError("ERR_PARSE_VALID_FROM", ErrorType.SPECIAL);
        outputRecord.addError(tmpError);
      }

      try {
        outputRecord.setSegmentValidFromUTC(conv.convertInputDateToUTC(validFromSegment + " 00:00:00"));
      } catch (ParseException ex) {
        // we could not parse the subscriptionID to be an integer
        RecordError tmpError = new RecordError("ERR_PARSE_VALID_FROM", ErrorType.SPECIAL);
        outputRecord.addError(tmpError);
      }

      try {
        outputRecord.setValidToUTC(conv.convertInputDateToUTC(validTo + " 23:59:59"));
      } catch (ParseException ex) {
        // we could not parse the subscriptionID to be an integer
        RecordError tmpError = new RecordError("ERR_PARSE_VALID_TO", ErrorType.SPECIAL);
        outputRecord.addError(tmpError);
      }

      try {
        outputRecord.setSegmentValidToUTC(conv.convertInputDateToUTC(validToSegment + " 23:59:59"));
      } catch (ParseException ex) {
        // we could not parse the subscriptionID to be an integer
        RecordError tmpError = new RecordError("ERR_PARSE_VALID_TO", ErrorType.SPECIAL);
        outputRecord.addError(tmpError);
      }

      // Adjust the out of range dates
      if (outputRecord.getValidFromUTC() < 0) {
        outputRecord.setValidFromUTC(0);
      }

      // Adjust the out of range dates
      if (outputRecord.getSegmentValidFromUTC() < 0) {
        outputRecord.setSegmentValidFromUTC(0);
      }

      // Check the length of the number
      if (number.isEmpty()) {
        // we could not parse the subscriptionID to be an integer
        RecordError tmpError = new RecordError("ERR_NUMBER_EMPTY", ErrorType.SPECIAL);
        outputRecord.addError(tmpError);
      }

      // Check the length of the number
      if (number.length() >= 24) {
        // we could not parse the subscriptionID to be an integer
        RecordError tmpError = new RecordError("ERR_NUMBER_TOO_LONG", ErrorType.SPECIAL);
        outputRecord.addError(tmpError);
      }

      // reset the price plan data
      pricePlan = null;
      validFrom = null;
      validTo = null;
    } // reset the service and number data
    else if (tmpData.startsWith("</account>")) {
      number = null;
    } // reset the service and number data
    else if (tmpData.startsWith("</customer>")) {
      serviceId = null;
      custId = null;

      // for safety
      inSegment = false;
      inTariff = false;
    }

    return outputRecord;
  }

  /**
   * This is called when a data record with errors is encountered. You should do
   * any processing here that you have to do for error records, e.g. statistics,
   * special handling, even error correction!
   *
   * The input adapter is not expected to provide any records here.
   *
   * @param r The record to work on
   * @return Modified record
   */
  @Override
  public IRecord procErrorRecord(IRecord r) {

    // The FlatFileInputAdapter is not able to create error records, so we
    // do not have to do anything for this
    return r;
  }

  /**
   * This is called when the synthetic trailer record is encountered, and has
   * the meaning that the stream is now finished. In this example, all we do is
   * pass the control back to the transactional layer.
   *
   * In models where record aggregation (records and sub records) is used, you
   * might want to check for any purged records here.
   *
   * @param r The record to work on
   * @return Modified record
   */
  @Override
  public IRecord procTrailer(IRecord r) {
    TrailerRecord tmpTrailer;

    // set the trailer record count
    tmpTrailer = (TrailerRecord) r;

    tmpTrailer.setRecordCount(StreamRecordNumber);
    return (IRecord) tmpTrailer;
  }

  /**
   * Order the list of files. This is can be overridden so that the sure may
   * define their own rules.
   *
   * @param dir The directory to scan
   * @param filter The filter we are using
   * @return A list of files to process, first in list gets processed first
   */
  @Override
  public String[] getOrderedFileListForProcessing(File dir, FilenameFilter filter) {
    String[] orgFileNames = super.getOrderedFileListForProcessing(dir, filter);
    Arrays.sort(orgFileNames);
    return orgFileNames;
  }
}
