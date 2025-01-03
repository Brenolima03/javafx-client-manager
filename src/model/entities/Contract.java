package model.entities;

import java.time.LocalDate;
import java.util.Base64;
import model.entities.Guarantee.GuaranteeType;

public class Contract {
  private int id;
  private int tenant;
  private int landlord;
  private LocalDate rentBeginning;
  private LocalDate rentEnd;
  private double rentValue;
  private double depositValue;
  private Guarantee guarantee;
  private String fileBase64;
  private double energyBill;
  private double waterBill;
  private LocalDate contractSigningDate;

  public Contract() {}

  public Contract(
    int id, int tenant, int landlord, LocalDate rentBeginning,
    LocalDate rentEnd, double rentValue, double depositValue,
    Guarantee guarantee, String fileBase64, double energyBill, 
    double waterBill, LocalDate contractSigningDate
  ) {
    this.id = id;
    this.tenant = tenant;
    this.landlord = landlord;
    this.rentBeginning = rentBeginning;
    this.rentEnd = rentEnd;
    this.rentValue = rentValue;
    this.depositValue = depositValue;
    this.guarantee = guarantee;
    this.fileBase64 = fileBase64;
    this.energyBill = energyBill;
    this.waterBill = waterBill;
    this.contractSigningDate = contractSigningDate;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getTenant() {
    return tenant;
  }

  public void setTenant(int tenant) {
    this.tenant = tenant;
  }

  public int getLandlord() {
    return landlord;
  }

  public void setLandlord(int landlord) {
    this.landlord = landlord;
  }

  public LocalDate getRentBeginning() {
    return rentBeginning;
  }

  public void setRentBeginning(LocalDate rentBeginning) {
    this.rentBeginning = rentBeginning;
  }

  public LocalDate getRentEnd() {
    return rentEnd;
  }

  public void setRentEnd(LocalDate rentEnd) {
    this.rentEnd = rentEnd;
  }

  public double getRentValue() {
    return rentValue;
  }

  public void setRentValue(double rentValue) {
    this.rentValue = rentValue;
  }

  public double getDepositValue() {
    return depositValue;
  }

  public void setDepositValue(double depositValue) {
    this.depositValue = depositValue;
  }

  public Guarantee getGuarantee() {
    return guarantee;
  }

  public void setGuarantee(Guarantee guarantee) {
    this.guarantee = guarantee;
  }

  public String getFileBase64() {
    return fileBase64;
  }

  public void setFileBase64(String fileBase64) {
    this.fileBase64 = fileBase64;
  }

  public double getEnergyBill() {
    return energyBill;
  }

  public void setEnergyBill(double energyBill) {
    this.energyBill = energyBill;
  }

  public double getWaterBill() {
    return waterBill;
  }

  public void setWaterBill(double waterBill) {
    this.waterBill = waterBill;
  }

  public LocalDate getContractSigningDate() {
    return contractSigningDate;
  }

  public void setContractSigningDate(LocalDate contractSigningDate) {
    this.contractSigningDate = contractSigningDate;
  }

  // Encoding and decoding methods for Base64
  public static String encodeFileToBase64(byte[] fileBytes) {
    return Base64.getEncoder().encodeToString(fileBytes);
  }

  public static byte[] decodeFileFromBase64(String fileBase64) {
    return Base64.getDecoder().decode(fileBase64);
  }

  @Override
  public String toString() {
    return "Contract [id=" + id + ", tenant=" + tenant + ", landlord="
      + landlord + ", rentBeginning=" + rentBeginning + ", rentEnd=" + rentEnd 
      + ", rentValue=" + rentValue + ", guarantee=" + guarantee 
      + ", energyBill=" + energyBill + ", waterBill=" + waterBill 
      + ", contractSigningDate=" + contractSigningDate + "]";
  }
}
