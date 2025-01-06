package com.model.entities;

import java.util.List;

public class Guarantee {
  private GuaranteeType guaranteeType;
  private List<String> guarantorNames;
  private boolean isGuarantorMarried;
  private String InstitutionName;
  private double depositValue;

  public Guarantee() {}

  public Guarantee(GuaranteeType guaranteeType, List<String> guarantorNames) {
    this.guaranteeType = guaranteeType;
    this.guarantorNames = guarantorNames;
  }

  public GuaranteeType getGuaranteeType() {
    return guaranteeType;
  }

  public void setGuaranteeType(GuaranteeType guaranteeType) {
    this.guaranteeType = guaranteeType;
  }

  public List<String> getGuarantorNames() {
    return guarantorNames;
  }

  public void setGuarantorNames(List<String> guarantorNames) {
    this.guarantorNames = guarantorNames;
  }

  public boolean getIsGuarantorMarried() {
    return isGuarantorMarried;
  }

  public void setIsGuarantorMarried(boolean isGuarantorMarried) {
    this.isGuarantorMarried = isGuarantorMarried;
  }

  public String getInstitutionName() {
    return InstitutionName;
  }

  public void setInstitutionName(String InstitutionName) {
    this.InstitutionName = InstitutionName;
  }

  public double getDepositValue() {
    return depositValue;
  }

  public void setDepositValue(double depositValue) {
    this.depositValue = depositValue;
  }

  public enum GuaranteeType {
    DEPOSIT,
    GUARANTOR,
    CAPITALIZATION_TITLE,
    BAIL_INSURANCE;

    @Override
    public String toString() {
      switch (this) {
        case DEPOSIT:
          return "Caução";
        case GUARANTOR:
          return "Fiador";
        case CAPITALIZATION_TITLE:
          return "Capitalização";
        case BAIL_INSURANCE:
          return "Seguro fiança";
        default:
          return super.toString();
      }
    }
  }
}
