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

import OpenRate.process.AbstractBestMatch;
import OpenRate.record.ChargePacket;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;
import java.util.ArrayList;

/**
 * Look up the zone result, category and type based on the B Number. The zone
 * result is used to calculate the zone for rating purposes, the category is
 * used for billing output information and markup, and the type is used for
 * markup.
 */
public class ZoneLookup extends AbstractBestMatch {

  // convenience variables

  private final int IDX_ZONE_RESULT = 0;    // Zone result used for rating
  private final int IDX_ZONE_CAT = 1;    // Category: World
  private final int IDX_ZONE_TYPE = 2;    // Fixed or mobile

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procValidRecord(IRecord r) {
    RecordError tmpError;
    ArrayList<String> ZoneValue;
    TyfonRecord CurrentRecord = (TyfonRecord) r;

    if ((CurrentRecord.RECORD_TYPE == TyfonRecord.VENTELO_DETAIL_RECORD)
            || (CurrentRecord.RECORD_TYPE == TyfonRecord.TELAVOX_DETAIL_RECORD)
            || (CurrentRecord.RECORD_TYPE == TyfonRecord.BAHNHOF_DETAIL_RECORD)
            || (CurrentRecord.RECORD_TYPE == TyfonRecord.FRAUD_DETAIL_RECORD)) {
      // Look up the destinations for the charge packets
      // Markup types have already been dealt with, just deal with the others
      if (CurrentRecord.isMarkup) {
        // Try to look up, warn if fails
        // ***************************** Info **********************************
        // Look up the Destination from the general list
        ZoneValue = getBestMatchWithChildData("Default", CurrentRecord.B_NumberNorm);

        if (isValidBestMatchResult(ZoneValue)) {
          // Write the information back into the record
          CurrentRecord.Destination = ZoneValue.get(IDX_ZONE_RESULT);
          CurrentRecord.Zone_Cat = ZoneValue.get(IDX_ZONE_CAT);
          CurrentRecord.Dest_Phone_Type = ZoneValue.get(IDX_ZONE_TYPE);
        } else {
          // no zone found, warn
          getPipeLog().warning("Could not find zone info for B Number <" + CurrentRecord.B_NumberNorm + ">");

          // Default the info - should not be used
          CurrentRecord.Destination = "Markup";
          CurrentRecord.Zone_Cat = "Markup";
          CurrentRecord.Dest_Phone_Type = "Markup";
        }

        // ****************************** CPs **********************************
        // Find the price group and place them into the charge packets
        for (int idx = 0; idx < CurrentRecord.getChargePacketCount(); idx++) {
          ChargePacket tmpCP = CurrentRecord.getChargePacket(idx);

          // Show the zone model we are using
          tmpCP.zoneModel = tmpCP.ratePlanName;
          tmpCP.zoneResult = "Markup";
          tmpCP.zoneInfo = "Markup";
        }
      } else {
        // ***************************** Info **********************************
        // Look up the Destination from the general list
        ZoneValue = getBestMatchWithChildData("Default", CurrentRecord.B_NumberNorm);

        if (isValidBestMatchResult(ZoneValue)) {
          // Write the information back into the record
          CurrentRecord.Destination = ZoneValue.get(IDX_ZONE_RESULT);
          CurrentRecord.Zone_Cat = ZoneValue.get(IDX_ZONE_CAT);
          CurrentRecord.Dest_Phone_Type = ZoneValue.get(IDX_ZONE_TYPE);
        } else {
          // no zone found, add an error to the record
          tmpError = new RecordError("ERR_ZONE_LOOKUP", ErrorType.SPECIAL);
          CurrentRecord.addError(tmpError);
        }

        // ****************************** CPs **********************************
        // Find the price group and place them into the charge packets
        for (int idx = 0; idx < CurrentRecord.getChargePacketCount(); idx++) {
          ChargePacket tmpCP = CurrentRecord.getChargePacket(idx);

          // Show the zone model we are using
          tmpCP.zoneModel = tmpCP.ratePlanName;
          ZoneValue = getBestMatchWithChildData(tmpCP.ratePlanName, CurrentRecord.B_NumberNorm);

          if (this.isValidBestMatchResult(ZoneValue)) {
            tmpCP.zoneResult = ZoneValue.get(IDX_ZONE_RESULT);
            tmpCP.zoneInfo = ZoneValue.get(IDX_ZONE_CAT);
          } else {
            // if this is a base product, error, otherwise turn the CP off
            if (tmpCP.priority == 0) {
              // base product
              CurrentRecord.addError(new RecordError("ERR_BASE_PROD_ZONE_MAP", ErrorType.DATA_NOT_FOUND));
            } else {
              // overlay product
              tmpCP.Valid = false;
            }
          }
        }
      }
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
