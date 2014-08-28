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
/* ========================== VERSION HISTORY =========================
 * $Log: ProvRecord.java,v $
 * Revision 1.4  2014-03-14 15:37:38  ian
 * Update loading to respect end dates
 *
 * Revision 1.3  2014/03/12 20:44:56  ian
 * Update loading to respect end dates
 *
 * Revision 1.2  2012/10/28 09:41:13  ian
 * Update to have subscriptionID
 *
 * Revision 1.1  2012-10-17 18:14:28  ian
 * Update for release
 *
 * ====================================================================
 */
package Provisioning;

import OpenRate.record.FlatRecord;
import OpenRate.record.RecordError;
import java.util.ArrayList;

/**
 * Record used for mapping Fixed FX format to UFIH Extended
 */
public class ProvRecord extends FlatRecord
{ 
  private String custId;
  private String serviceId;
  private String number;
  private String pricePlan;
  private String validFrom;
  private String validTo;
  private long   validFromUTC;
  private long   validToUTC;
  private String segmentValidFrom;
  private String segmentValidTo;
  private long   segmentValidFromUTC;
  private long   segmentValidToUTC;

 /**
  * Return the dump-ready data
  * 
  * @return The dump strings
  */
  @Override
  public ArrayList<String> getDumpInfo()
  {
    int tmpErrorCount;
    int i;
    RecordError tmpError;
    ArrayList<String> tmpDumpList;
    tmpDumpList = new ArrayList<>();

    // Get the error count
    tmpErrorCount = this.getErrors().size();
    
    tmpDumpList.add("============ DETAIL RECORD ============");
    tmpDumpList.add("  Record Number   = <" + this.RecordNumber + ">");
    tmpDumpList.add("  custId          = <" + getCustId() + ">");
    tmpDumpList.add("  serviceId       = <" + getServiceId() + ">");
    tmpDumpList.add("  phone number    = <" + getNumber() + ">");
    tmpDumpList.add("  price plan      = <" + getPricePlan() + ">");
    tmpDumpList.add("  valid from      = <" + getValidFrom() + ">");
    tmpDumpList.add("  valid to        = <" + getValidTo() + ">");
    tmpDumpList.add("  valid from UTC  = <" + getValidFromUTC() + ">");
    tmpDumpList.add("  valid to UTC    = <" + getValidToUTC() + ">");
    tmpDumpList.add("  segment from    = <" + getSegmentValidFrom() + ">");
    tmpDumpList.add("  segment to      = <" + getSegmentValidTo() + ">");
    tmpDumpList.add("  segment from UTC= <" + getSegmentValidFromUTC() + ">");
    tmpDumpList.add("  segment to UTC  = <" + getSegmentValidToUTC() + ">");

    tmpDumpList.add("  Errors          = <" + this.getErrors().size() + ">");
    if (tmpErrorCount>0)
    {
      tmpDumpList.add("-------------- ERRORS ----------------");
      for (i = 0 ; i < this.getErrors().size() ; i++)
      {
        tmpError = (RecordError) this.getErrors().get(i);
        tmpDumpList.add("    Error           = <" + tmpError.getMessage() + "> (" + tmpError.getModuleName() + ")");
      }
    }

    return tmpDumpList;
  }

  /**
   * @return the custId
   */
  public String getCustId() {
    return custId;
  }

  /**
   * @param custId the custId to set
   */
  public void setCustId(String custId) {
    this.custId = custId;
  }

  /**
   * @return the number
   */
  public String getNumber() {
    return number;
  }

  /**
   * @param number the number to set
   */
  public void setNumber(String number) {
    this.number = number;
  }

  /**
   * @return the pricePlan
   */
  public String getPricePlan() {
    return pricePlan;
  }

  /**
   * @param pricePlan the pricePlan to set
   */
  public void setPricePlan(String pricePlan) {
    this.pricePlan = pricePlan;
  }

  /**
   * @return the validFrom
   */
  public String getValidFrom() {
    return validFrom;
  }

  /**
   * @param validFrom the validFrom to set
   */
  public void setValidFrom(String validFrom) {
    this.validFrom = validFrom;
  }

  /**
   * @return the validTo
   */
  public String getValidTo() {
    return validTo;
  }

  /**
   * @param validTo the validTo to set
   */
  public void setValidTo(String validTo) {
    this.validTo = validTo;
  }

  /**
   * @return the validFromUTC
   */
  public long getValidFromUTC() {
    return validFromUTC;
  }

  /**
   * @param validFromUTC the validFromUTC to set
   */
  public void setValidFromUTC(long validFromUTC) {
    this.validFromUTC = validFromUTC;
  }

  /**
   * @return the validToUTC
   */
  public long getValidToUTC() {
    return validToUTC;
  }

  /**
   * @param validToUTC the validToUTC to set
   */
  public void setValidToUTC(long validToUTC) {
    this.validToUTC = validToUTC;
  }

  /**
   * @return the serviceId
   */
  public String getServiceId() {
    return serviceId;
  }

  /**
   * @param serviceId the serviceId to set
   */
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  /**
   * @return the segmentValidFrom
   */
  public String getSegmentValidFrom() {
    return segmentValidFrom;
  }

  /**
   * @param segmentValidFrom the segmentValidFrom to set
   */
  public void setSegmentValidFrom(String segmentValidFrom) {
    this.segmentValidFrom = segmentValidFrom;
  }

  /**
   * @return the segmentValidTo
   */
  public String getSegmentValidTo() {
    return segmentValidTo;
  }

  /**
   * @param segmentValidTo the segmentValidTo to set
   */
  public void setSegmentValidTo(String segmentValidTo) {
    this.segmentValidTo = segmentValidTo;
  }

  /**
   * @return the segmentValidFromUTC
   */
  public long getSegmentValidFromUTC() {
    return segmentValidFromUTC;
  }

  /**
   * @param segmentValidFromUTC the segmentValidFromUTC to set
   */
  public void setSegmentValidFromUTC(long segmentValidFromUTC) {
    this.segmentValidFromUTC = segmentValidFromUTC;
  }

  /**
   * @return the segmentValidToUTC
   */
  public long getSegmentValidToUTC() {
    return segmentValidToUTC;
  }

  /**
   * @param segmentValidToUTC the segmentValidToUTC to set
   */
  public void setSegmentValidToUTC(long segmentValidToUTC) {
    this.segmentValidToUTC = segmentValidToUTC;
  }
}
