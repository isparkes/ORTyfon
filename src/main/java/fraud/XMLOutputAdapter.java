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

import OpenRate.adapter.file.FlatFileOutputAdapter;
import OpenRate.exception.ProcessingException;
import OpenRate.record.FlatRecord;
import OpenRate.record.HeaderRecord;
import OpenRate.record.IRecord;
import Tyfon.TyfonRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * The Output Adapter is responsible for writing the completed records to the
 * target file.
 */
public class XMLOutputAdapter extends FlatFileOutputAdapter {

  // Marks if the file has output or not
  private final HashMap<Integer, Boolean> fileHasContent = new HashMap<>();

  @Override
  public void openValidFile(String filename) {
    super.openValidFile(filename);

    try {
      getValidWriter().write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
              + "<alerts>\n");
    } catch (IOException ex) {
      getPipeLog().error("Exception in module <" + getSymbolicName() + ">, <" + ex.getMessage() + ">");
    }
  }

  @Override
  public int closeFiles(int TransactionNumber) {
    try {
      getValidWriter().write("</alerts>");
    } catch (IOException ex) {
      getPipeLog().error("Exception in module <" + getSymbolicName() + ">, <" + ex.getMessage() + ">");
    }

    return super.closeFiles(TransactionNumber);
  }

  @Override
  public Collection<IRecord> procValidRecord(IRecord r) {
    FlatRecord tmpOutRecord;
    TyfonRecord CurrentRecord;

    Collection<IRecord> Outbatch;
    Outbatch = new ArrayList<>();

    CurrentRecord = (TyfonRecord) r;

    // We only transform the detail records, and leave the others alone
    if (CurrentRecord.RECORD_TYPE == TyfonRecord.FRAUD_DETAIL_RECORD) {
      for (FraudAlert alert : CurrentRecord.alerts) {
        tmpOutRecord = new FlatRecord();
        tmpOutRecord.setData("\t<alert>\n"
                + wrapValue("customerId", CurrentRecord.CustIDA)
                + wrapValue("number", CurrentRecord.A_Number)
                + wrapValue("limit", alert.getThresholdName())
                + wrapValue("limit_value", alert.getNotificationLimit())
                + wrapValue("limit_description", alert.getDescription())
                + wrapValue("balance", alert.getBalance())
                + "\t</alert>");
        Outbatch.add((IRecord) tmpOutRecord);

        // Mark that this is not an empty file
        if (fileHasContent.get(getTransactionNumber()) == true) {
          fileHasContent.put(getTransactionNumber(), false);
        }
      }
    }

    return Outbatch;
  }

  @Override
  public Collection<IRecord> procErrorRecord(IRecord r) {
    return null;
  }

  /**
   * Wrap a value as a level 2 xml tag.
   *
   * @param tag The tag name
   * @param value The value to wrap
   * @return The wrapped value
   */
  private String wrapValue(String tag, String value) {
    String result;

    result = "\t\t<" + tag + ">" + value + "</" + tag + ">\n";

    return result.replaceAll("&", "&amp;");
  }

  /**
   * Wrap a value as a level 2 xml tag.
   *
   * @param tag The tag name
   * @param value The value to wrap
   * @return The wrapped value
   */
  private String wrapValue(String tag, int value) {
    String result;

    result = "\t\t<" + tag + ">" + value + "</" + tag + ">\n";

    return result.replaceAll("&", "&amp;");
  }

  /**
   * Wrap a value as a level 2 xml tag.
   *
   * @param tag The tag name
   * @param value The value to wrap
   * @return The wrapped value
   */
  private String wrapValue(String tag, double value) {
    String result;

    result = "\t\t<" + tag + ">" + value + "</" + tag + ">\n";

    return result.replaceAll("&", "&amp;");
  }

  /**
   * Checks if the valid output file is empty. This method is intended to be
   * overwritten in the case that you wish to modify the behaviour of the output
   * file deletion.
   *
   * The default behaviour is that we check to see if any bytes have been
   * written to the output file, but sometimes this is not the right way, for
   * example if a file has a header/trailer but no detail records.
   *
   * @param transactionNumber The number of the transaction to check for
   * @return true if the file is empty, otherwise false
   */
  @Override
  public boolean getOutputFileEmpty(int transactionNumber) {
    boolean fileStatus = fileHasContent.get(transactionNumber);
    fileHasContent.remove(transactionNumber);
    return fileStatus;
  }

  /**
   * Process the stream header. Set up the file content list based on the
   * transaction number held in the header record.
   *
   * @param r The record we are working on
   * @return The processed record
   * @throws ProcessingException
   */
  @Override
  public IRecord procHeader(IRecord r) throws ProcessingException {
    super.procHeader(r);

    HeaderRecord tmpHeader = (HeaderRecord) r;
    int tmpTransNumber = tmpHeader.getTransactionNumber();

    fileHasContent.put(tmpTransNumber, true);

    return r;
  }
}
