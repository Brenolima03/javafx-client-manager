package com.model.entities;

public class Estate {
  private int id;
  private int tenantId;
  private int landlordId;
  private String address;
  private int number;
  private String neighborhood;
  private String city;
  private String state;
  private String description;

  public Estate() {}

  public Estate(
    int id, int tenantId, int landlordId, String address, int number,
    String neighborhood, String city, String state, String description
  ) {
    this.id = id;
    this.tenantId = tenantId;
    this.landlordId = landlordId;
    this.address = address;
    this.number = number;
    this.neighborhood = neighborhood;
    this.city = city;
    this.state = state;
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getTenantId() {
    return tenantId;
  }

  public void setTenantId(int tenantId) {
    this.tenantId = tenantId;
  }

  public int getLandlordId() {
    return landlordId;
  }

  public void setLandlordId(int landlordId) {
    this.landlordId = landlordId;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public String getNeighborhood() {
    return neighborhood;
  }

  public void setNeighborhood(String neighborhood) {
    this.neighborhood = neighborhood;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + this.id;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    final Estate other = (Estate) obj;
    return this.id == other.id;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Estate{");
    sb.append("id=").append(id);
    sb.append(", tenantId=").append(tenantId);
    sb.append(", landlordId=").append(landlordId);
    sb.append(", address=").append(address);
    sb.append(", number=").append(number);
    sb.append(", neighborhood=").append(neighborhood);
    sb.append(", city=").append(city);
    sb.append(", state=").append(state);
    sb.append(", description=").append(description);
    sb.append('}');
    return sb.toString();
  }
}
