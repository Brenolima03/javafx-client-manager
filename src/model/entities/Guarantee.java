package model.entities;

public class Guarantee {
  private GuaranteeType guaranteeType;
  private boolean isSponsorMarried;
  private String partnerName;
  private String guarantorOrInstitutionName;
  private double depositValue;

  public Guarantee(GuaranteeType guaranteeType) {
    this.guaranteeType = guaranteeType;
  }

  public GuaranteeType getGuaranteeType() {
    return guaranteeType;
  }

  public void setGuaranteeType(GuaranteeType guaranteeType) {
	  this.guaranteeType = guaranteeType;
  }

  public boolean isSponsorMarried() {
    return isSponsorMarried;
  }

  public void setSponsorMarried(boolean isSponsorMarried) {
    this.isSponsorMarried = isSponsorMarried;
  }

  public String getPartnerName() {
    return partnerName;
  }

  public void setPartnerName(String partnerName) {
    this.partnerName = partnerName;
  }

  public String getGuarantorOrInstitutionName() {
    return guarantorOrInstitutionName;
  }

  public void setGuarantorOrInstitutionName(String guarantorOrInstitutionName) {
    this.guarantorOrInstitutionName = guarantorOrInstitutionName;
  }

  public double getDepositValue() {
    return depositValue;
  }

  public void setDepositValue(double depositValue) {
    this.depositValue = depositValue;
  }

  public enum GuaranteeType {
    DEPOSIT, GUARANTOR, CAPITALIZATION_TITLE, BAIL_INSURANCE;

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
        return super.toString(); // Fallback to the default name() if needed
      }
    }
  }
}
