/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fraud;

/**
 * 
 * @author ian
 */
public class FraudAlert {
  private String thresholdName;
  private double balance;
  private double notificationLimit;
  private String description;

  /**
   * @return the thresholdName
   */
  public String getThresholdName() {
    return thresholdName;
  }

  /**
   * @param thresholdName the thresholdName to set
   */
  public void setThresholdName(String thresholdName) {
    this.thresholdName = thresholdName;
  }

  /**
   * @return the balance
   */
  public double getBalance() {
    return balance;
  }

  /**
   * @param balance the balance to set
   */
  public void setBalance(double balance) {
    this.balance = balance;
  }

  /**
   * @return the notificationLimit
   */
  public double getNotificationLimit() {
    return notificationLimit;
  }

  /**
   * @param notificationLimit the notificationLimit to set
   */
  public void setNotificationLimit(double notificationLimit) {
    this.notificationLimit = notificationLimit;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
