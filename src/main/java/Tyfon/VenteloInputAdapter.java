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
 * $Log: VenteloInputAdapter.java,v $
 * Revision 1.6  2014-03-12 20:44:56  ian
 * Update loading to respect end dates
 *
 * Revision 1.5  2012/12/01 00:28:35  ian
 * Recycle handling
 *
 * Revision 1.4  2012/11/30 21:28:08  ian
 * Update
 *
 * Revision 1.3  2012-10-17 18:14:23  ian
 * Update for release
 *
 * Revision 1.2  2012-07-17 22:32:50  ian
 * WIP
 *
 * ====================================================================
 */
package Tyfon;

import OpenRate.adapter.file.FlatFileInputAdapter;
import OpenRate.record.FlatRecord;
import OpenRate.record.IRecord;

/**
 * Instace of the input adapter for the Ventelo traffic type.
 *
 * @author TGDSPIA1
 */
public class VenteloInputAdapter extends FlatFileInputAdapter
{
  /**
   * CVS version info - Automatically captured and written to the Framework
   * Version Audit log at Framework startup. For more information
   * please <a target='new' href='http://www.open-rate.com/wiki/index.php?title=Framework_Version_Map'>click here</a> to go to wiki page.
   */
  public static String CVS_MODULE_INFO = "OpenRate, $RCSfile: VenteloInputAdapter.java,v $, $Revision: 1.6 $, $Date: 2014-03-12 20:44:56 $";

  private int IntRecordNumber;
 
 /**
  * Constructor for CustomizeInputAdapter.
  */
  public VenteloInputAdapter()
  {
      super();      
  }

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------  
 /**
  * This is called when the synthetic Header record is encountered, and has the
  * meanining that the stream is starting. In this example we have nothing to do
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
    
    // Create the new record
    tmpDataRecord = new TyfonRecord();
    
    // Check the record type - deal with each of the types separately
    if (tmpFlatRecord.getData().startsWith(TyfonRecord.VENTELO_TYPE_DETAIL))
    {  
      // Normal detail record
      tmpDataRecord.mapVenteloDetailRecord(tmpFlatRecord.getData());
      IntRecordNumber++;
      tmpDataRecord.RecordNumber = IntRecordNumber;
    }
    else if (tmpFlatRecord.getData().startsWith(TyfonRecord.VENTELO_TYPE_HEADER))
    {
      // Header record
      tmpDataRecord.mapVenteloHeaderRecord(tmpFlatRecord.getData());
    }
    else if (tmpFlatRecord.getData().startsWith(TyfonRecord.VENTELO_TYPE_TRAILER))
    {
      // Trailer record
      tmpDataRecord.mapVenteloTrailerRecord(tmpFlatRecord.getData());
    }
    else if (tmpFlatRecord.getData().startsWith(TyfonRecord.RECYCLE_TAG))
    {
      // Recycled detail record
      tmpDataRecord.mapVenteloDetailRecord(tmpFlatRecord.getData());
      IntRecordNumber++;
      tmpDataRecord.RecordNumber = IntRecordNumber;
    }
    else
    {
      // Unknown record
      tmpDataRecord.mapUnknownRecord(tmpFlatRecord.getData());
    }
    
    // Return the modified record in the Common record format (IRecord)
    return (IRecord) tmpDataRecord;
  }

 /**
  * This is called when a data record with errors is encountered. You should do
  * any processing here that you have to do for error records, e.g. stratistics,
  * special handling, even error correction!
  * 
  * The input adapter is not expected to provide any records here.
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
  * meanining that the stream is now finished. In this example, all we do is 
  * pass the control back to the transactional layer.
  */
  @Override
  public IRecord procTrailer(IRecord r)
  {

    return r;
  }
}
