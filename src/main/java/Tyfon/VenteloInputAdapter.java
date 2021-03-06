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

import OpenRate.adapter.file.FlatFileInputAdapter;
import OpenRate.record.FlatRecord;
import OpenRate.record.IRecord;

/**
 * Instance of the input adapter for the Ventelo traffic type.
 *
 * @author TGDSPIA1
 */
public class VenteloInputAdapter extends FlatFileInputAdapter
{
  private int IntRecordNumber;
 
  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------  
 
  @Override
  public IRecord procHeader(IRecord r)
  {
    IntRecordNumber = 0;
    
    return r;
  }

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

  @Override
  public IRecord procErrorRecord(IRecord r)
  {
    // The FlatFileInputAdapter is not able to create error records, so we
    // do not have to do anything for this
    return r;
  }

  @Override
  public IRecord procTrailer(IRecord r)
  {

    return r;
  }
}
