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

import OpenRate.process.AbstractRegexMatch;
import OpenRate.record.IRecord;

/**
 * This Module manages the call scenarios which we are rating for. The following
 * cases are managed: - Certain special numbers are markup only (in order to
 * manage these more easily than tracking prices individually). This managed in
 * this module so that we do not have to deal with unknown prefixes, we just
 * treat these destinations as "opaque" numbers and do not try to look into them
 */
public class CallScenarioLookup extends AbstractRegexMatch {

  // used to perform the lookup - defined here for performance reasons
  String[] searchParams = new String[1];

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procValidRecord(IRecord r) {
    TyfonRecord CurrentRecord = (TyfonRecord) r;

    if ((CurrentRecord.RECORD_TYPE == TyfonRecord.VENTELO_DETAIL_RECORD)
            || (CurrentRecord.RECORD_TYPE == TyfonRecord.TELAVOX_DETAIL_RECORD)
            || (CurrentRecord.RECORD_TYPE == TyfonRecord.BAHNHOF_DETAIL_RECORD)) {
      // check if this is one of the markup scenarios
      searchParams[0] = CurrentRecord.supplier;
      String markupType = getRegexMatch("Default", searchParams);

      // If we got a match, process it
      if (isValidRegexMatchResult(markupType)) {
        // Mark that we are marking up and set the key
        CurrentRecord.markupType = markupType;
        CurrentRecord.isMarkup = true;
      } else {
        // Set that we are not marking up
        CurrentRecord.markupType = "";
        CurrentRecord.isMarkup = false;
      }
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
