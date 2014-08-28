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
/* ========================== VERSION HISTORY =========================
 * $Log: CustomerLookup.java,v $
 * Revision 1.8  2014-05-24 11:21:25  ian
 * First version fraud
 *
 * Revision 1.7  2014-03-18 20:24:20  ian
 * Added selection of correct tariff from segment
 *
 * Revision 1.6  2014/03/12 20:44:55  ian
 * Update loading to respect end dates
 *
 * Revision 1.5  2012/11/30 21:28:08  ian
 * Update
 *
 * Revision 1.4  2012/10/28 09:41:12  ian
 * Update to have subscriptionID
 *
 * Revision 1.3  2012-10-17 18:14:23  ian
 * Update for release
 *
 * Revision 1.2  2012-07-17 22:32:49  ian
 * WIP
 *
 * ====================================================================
 */
package Tyfon;

import OpenRate.exception.ProcessingException;
import OpenRate.lang.AuditSegment;
import OpenRate.lang.CustProductInfo;
import OpenRate.lang.ProductList;
import OpenRate.process.AbstractCustomerLookupAudited;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

/**
 * Look up the customer based on the A Number.
 *
 * @author TGDSPIA1
 */
public class CustomerLookup extends AbstractCustomerLookupAudited {

  /**
   * CVS version info - Automatically captured and written to the Framework
   * Version Audit log at Framework startup. For more information please <a
   * target='new'
   * href='http://www.open-rate.com/wiki/index.php?title=Framework_Version_Map'>click
   * here</a> to go to wiki page.
   */
  public static String CVS_MODULE_INFO = "OpenRate, $RCSfile: CustomerLookup.java,v $, $Revision: 1.8 $, $Date: 2014-05-24 11:21:25 $";

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  /**
   * This is called when a valid detail record is encountered.
   * @return 
   * @throws OpenRate.exception.ProcessingException 
   */
  @Override
  public IRecord procValidRecord(IRecord r) throws ProcessingException {
    TyfonRecord CurrentRecord;

    CurrentRecord = (TyfonRecord) r;

    if ((CurrentRecord.RECORD_TYPE == TyfonRecord.VENTELO_DETAIL_RECORD) ||
        (CurrentRecord.RECORD_TYPE == TyfonRecord.TELAVOX_DETAIL_RECORD) ||
        (CurrentRecord.RECORD_TYPE == TyfonRecord.FRAUD_DETAIL_RECORD))
    {
      try
      {
        // Get the A customer identification
        CurrentRecord.CustIDA = getCustId(CurrentRecord.A_Number, CurrentRecord.UTCEventDate);

        if (CurrentRecord.CustIDA == null)
        {
          CurrentRecord.addError(new RecordError("ERR_CUST_NOT_FOUND", ErrorType.DATA_NOT_FOUND));
        }
        else
        {
          // Get the audit segment we need
          AuditSegment tmpAudSeg = this.getAuditSegment(CurrentRecord.CustIDA, CurrentRecord.UTCEventDate);
          
          // Get the proucts from it
          ProductList tmpProductList;
          CustProductInfo tmpCPI;

          tmpProductList = this.getProductList(tmpAudSeg,null);

          if ((tmpProductList == null) || (tmpProductList.getProductCount() == 0))
          {
            // did not find any customer information
            CurrentRecord.addError(new RecordError("ERR_TARIFF_NOT_FOUND", ErrorType.DATA_NOT_FOUND));
          }
          else
          {
            // Get the product we are using
            boolean prodFound = false;
            for (int idx = 0 ; idx < tmpProductList.getProductCount() ; idx++)
            {
              tmpCPI = tmpProductList.getProduct(idx);
              if (tmpCPI.getUTCValidFrom() <= CurrentRecord.UTCEventDate &&
                  tmpCPI.getUTCValidTo() > CurrentRecord.UTCEventDate)
              {
                CurrentRecord.UsedProduct = tmpCPI.getProductID();
                CurrentRecord.subscriptionID = tmpCPI.getSubID();
                prodFound = true;
              }
            }
            
            if (prodFound == false)
            {
              // did not find any customer information
              CurrentRecord.addError(new RecordError("ERR_TARIFF_NOT_FOUND", ErrorType.DATA_NOT_FOUND));
            }
          }
        }
      } 
      catch (ProcessingException e)
      {
        // error detected,
        getPipeLog().error(e.getMessage());
        RecordError tmpError = new RecordError("ERR_CUSTOMER_LOOKUP", ErrorType.SPECIAL);
        tmpError.setErrorDescription(e.getMessage());
        CurrentRecord.addError(tmpError);
      }
    }
    
    return (IRecord) CurrentRecord;
  }

  /**
   * @return 
   * This is called when an error detail record is encountered.
   */
  @Override
  public IRecord procErrorRecord(IRecord r)
  {
    return null;
  }
}
