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
package fraud;

import OpenRate.adapter.file.FlatFileInputAdapter;
import OpenRate.record.FlatRecord;
import OpenRate.record.IRecord;
import Tyfon.TyfonRecord;

/**
 * Input adapter for fraud records. Formatted like:
 *
 * A B C D E F G H I J K L
 * "2014-02-25 16:01:56","2014-02-25
 * 16:02:08","12","11","0851781301","0854601162","N","N","OUTBOUND","external-proxy","ANSWER/NORMAL_CLEARING","ANSWERED"
 *
 * A: Starttid (start time) B: Sluttid (end time) C: Telefontid i sekunder
 * (calltime ink signaling in seconds) D: Samtalstid i sekunder (actual billable
 * call time) E: A-nummer F: B-nummer G: A-nummer är dolt (CLIR) Y/N (hidden nr)
 * H: Vidarekopplat samtal Y/N (redirected call) I: Samtalsriktning sett från
 * oss (Tyfon) (call direction from our veiw) J: Kontaktad SIP-peer ( sip peer)
 * K: Telefonresultat (dialresult) L: Samtalsresultat (disposition)
 *
 * @author ian
 */
public class FraudInputAdapter extends FlatFileInputAdapter {

  private int IntRecordNumber = 0;

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------  
  @Override
  public IRecord procHeader(IRecord r) {
    IntRecordNumber = 0;

    return r;
  }

  @Override
  public IRecord procValidRecord(IRecord r) {

    TyfonRecord tmpDataRecord;
    FlatRecord tmpFlatRecord;

    /* The source of the record is FlatRecord, because we are using the
     * FlatFileInputAdapter as the source of the records. We cast the record
     * to this to extract the data, and then create the target record type
     * (CustomizedRecord) and cast this back to the generic class before passing
     * back
     */
    tmpFlatRecord = (FlatRecord) r;

    tmpDataRecord = new TyfonRecord();

    tmpDataRecord.mapFraudDetailRecord(tmpFlatRecord.getData());
    IntRecordNumber++;
    tmpDataRecord.RecordNumber = IntRecordNumber;

    // Return the modified record in the Common record format (IRecord)
    return (IRecord) tmpDataRecord;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    // The FlatFileInputAdapter is not able to create error records, so we
    // do not have to do anything for this
    return r;
  }

  @Override
  public IRecord procTrailer(IRecord r) {
    return r;
  }
}
