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
/* ========================== VERSION HISTORY =========================
 * $Log: FraudInputAdapter.java,v $
 * Revision 1.2  2014-05-24 11:21:25  ian
 * First version fraud
 *
 * Revision 1.1  2014-05-23 07:22:47  ian
 * First version fraud WIP
 * ====================================================================
 */
package fraud;

import OpenRate.adapter.file.FlatFileInputAdapter;
import OpenRate.record.FlatRecord;
import OpenRate.record.IRecord;
import Tyfon.TyfonRecord;

/**
 * Input adapter for fraud records. Formatted like:
 * 
 * A                     B                     C    D    E            F            G   H   I          J                K                        L
 * "2014-02-25 16:01:56","2014-02-25 16:02:08","12","11","0851781301","0854601162","N","N","OUTBOUND","external-proxy","ANSWER/NORMAL_CLEARING","ANSWERED"
 * 
 * A: Starttid (start time)
 * B: Sluttid  (end time)
 * C: Telefontid i sekunder   (calltime ink signaling in seconds)
 * D: Samtalstid i sekunder   (actual billable call time)
 * E: A-nummer
 * F: B-nummer
 * G: A-nummer är dolt (CLIR) Y/N  (hidden nr)
 * H: Vidarekopplat samtal Y/N   (redirected call)
 * I: Samtalsriktning sett från oss (Tyfon)   (call direction from our veiw)
 * J: Kontaktad SIP-peer  ( sip peer)
 * K: Telefonresultat (dialresult)
 * L: Samtalsresultat (disposition)
 * 
 * @author ian
 */
public class FraudInputAdapter extends FlatFileInputAdapter
{
  /**
   * CVS version info - Automatically captured and written to the Framework
   * Version Audit log at Framework startup. For more information
   * please <a target='new' href='http://www.open-rate.com/wiki/index.php?title=Framework_Version_Map'>click here</a> to go to wiki page.
   */
  public static String CVS_MODULE_INFO = "OpenRate, $RCSfile: FraudInputAdapter.java,v $, $Revision: 1.2 $, $Date: 2014-05-24 11:21:25 $";

  private int IntRecordNumber = 0;
 
  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------  
 /**
  * This is called when the synthetic Header record is encountered, and has the
  * meanining that the stream is starting. In this example we have nothing to do
  * 
  * @return processed header
  */
  @Override
  public IRecord procHeader(IRecord r)
  {
    IntRecordNumber = 0;
    
    return r;
  }

 /**
  * This is called when a data record is encountered. You should do any normal
  * processing here. For the input adapter, we probably want to change the 
  * record type from FlatRecord to the record(s) type that we will be using in
  * the processing pipeline.
  *
  * This is also the location for accumulating records into logical groups
  * (that is records with sub records) and placing them in the pipeline as
  * they are completed. If you receive a sub record, simply return a null record
  * in this method to indicate that you are handling it, and that it will be
  * purged at a later date.
  * 
  * @return processed valid record
  */
  @Override
  public IRecord procValidRecord(IRecord r)
  {

    TyfonRecord tmpDataRecord;
    FlatRecord tmpFlatRecord;

   /* The source of the record is FlatRecord, because we are using the
    * FlatFileInputAdapter as the source of the records. We cast the record
    * to this to extract the data, and then create the target record type
    * (CustomizedRecord) and cast this back to the generic class before passing
    * back
    */
    tmpFlatRecord = (FlatRecord)r;
    
    tmpDataRecord = new TyfonRecord();
    
    tmpDataRecord.mapFraudDetailRecord(tmpFlatRecord.getData());
    IntRecordNumber++;
    tmpDataRecord.RecordNumber = IntRecordNumber;
    
    // Return the modified record in the Common record format (IRecord)
    return (IRecord) tmpDataRecord;
  }

 /**
  * This is called when a data record with errors is encountered. You should do
  * any processing here that you have to do for error records, e.g. statistics,
  * special handling, even error correction!
  * 
  * The input adapter is not expected to provide any records here.
  * 
  * @return processed error record
  */
  @Override
  public IRecord procErrorRecord(IRecord r)
  {
    // The FlatFileInputAdapter is not able to create error records, so we
    // do not have to do anything for this
    return r;
  }

 /**
  * This is called when the synthetic trailer record is encountered, and has the
  * meaning that the stream is now finished. In this example, all we do is 
  * pass the control back to the transactional layer.
  * 
  * @return processed trailer record
  */
  @Override
  public IRecord procTrailer(IRecord r)
  {
    return r;
  }
}
