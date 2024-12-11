package model.entities;

import java.util.Base64;

public class Contract {
  private int id;
  private Client tenant;
  private Client landlord;
  private String fileBase64;

  public Contract() {}

  public Contract(int id, Client tenant, Client landlord, String fileBase64) {
    this.id = id;
    this.tenant = tenant;
    this.landlord = landlord;
    this.fileBase64 = fileBase64;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Client getTenant() {
    return tenant;
  }

  public void setTenant(Client tenant) {
    this.tenant = tenant;
  }

  public Client getLandlord() {
    return landlord;
  }

  public void setLandlord(Client landlord) {
    this.landlord = landlord;
  }

  public String getFileBase64() {
    return fileBase64;
  }

  public void setFileBase64(String fileBase64) {
    this.fileBase64 = fileBase64;
  }

  // Method to convert file from byte[] to Base64 string (for saving images)
  public static String encodeFileToBase64(byte[] fileBytes) {
    return Base64.getEncoder().encodeToString(fileBytes);
  }

  // Method to decode Base64 string back to byte[] (for reading files)
  public static byte[] decodeFileFromBase64(String fileBase64) {
    return Base64.getDecoder().decode(fileBase64);
  }
}
