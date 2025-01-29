package com.model.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client {
  private int id;
  private String name;
  private String cpfCnpj;
  private String issuingOrganization;
  private String rg;
  private LocalDate birthDate;
  private List<Integer> contracts = new ArrayList<>();
  private String telephone;
  private ClientType clientType;
  private MaritalStatus maritalStatus;
  private String address;
  private String nationality;
  private String profession;
  private String neighborhood;
  private String city;
  private String state;
  private String zip;

  public enum ClientType {
    TENANT,
    LANDLORD;

    @Override
    public String toString() {
      return switch (this) {
        case TENANT -> "Locatário";
        case LANDLORD -> "Locador";
        default -> super.toString();
      }; // Fallback to the default name() if needed
    }
  }

  public enum MaritalStatus {
    SINGLE,
    MARRIED,
    DIVORCED,
    WIDOWED;

    @Override
    public String toString() {
      return switch (this) {
        case SINGLE -> "solteiro(a)";
        case MARRIED -> "casado(a)";
        case DIVORCED -> "divorciado(a)";
        case WIDOWED -> "viúvo(a)";
        default -> super.toString();
      }; // Fallback to the default name() if needed
    }
  }

  public Client() {}

  public Client(
    int id, String name, String cpfCnpj, List<Integer> contracts, String rg,
    String issuingOrganization, LocalDate birthDate, String telephone,
    ClientType clientType, MaritalStatus maritalStatus, String address,
    String nationality, String profession, String neighborhood, String city,
    String state, String zip
  ) {
    this.id = id;
    this.name = name;
    this.cpfCnpj = cpfCnpj;
    this.contracts = contracts;
    this.rg = rg;
    this.issuingOrganization = issuingOrganization;
    this.birthDate = birthDate;
    this.telephone = telephone;
    this.clientType = (clientType != null) ? clientType : ClientType.TENANT;
    this.maritalStatus = maritalStatus;
    this.address = address;
    this.nationality = nationality;
    this.profession = profession;
    this.neighborhood = neighborhood;
    this.city = city;
    this.state = state;
    this.zip = zip;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCpfCnpj() {
    return cpfCnpj;
  }

  public void setCpfCnpj(String cpfCnpj) {
    this.cpfCnpj = cpfCnpj;
  }

  public String getRg() {
    return rg;
  }

  public void setRg(String rg) {
    this.rg = rg;
  }

  public String getIssuingOrganization() {
    return issuingOrganization;
  }

  public void setIssuingOrganization(String issuingOrganization) {
    this.issuingOrganization = issuingOrganization;
  }

  public List<Integer> getContracts() {
    return contracts;
  }

  public void setContracts(List<Integer> contracts) {
    this.contracts = contracts;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public String getTelephone() {
    return telephone;
  }

  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  public ClientType getClientType() {
    return clientType;
  }

  public void setClientType(ClientType clientType) {
    this.clientType = clientType;
  }

  public MaritalStatus getMaritalStatus() {
    return maritalStatus;
  }

  public void setMaritalStatus(MaritalStatus maritalStatus) {
    this.maritalStatus = maritalStatus;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getNationality() {
    return nationality;
  }

  public void setNationality(String nationality) {
    this.nationality = nationality;
  }

  public String getProfession() {
    return profession;
  }

  public void setProfession(String profession) {
    this.profession = profession;
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

  public String getZip() {
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Client other = (Client) obj;
    if (id == 0) {
      if (other.id != 0)
        return false;
    } else if (id != other.id)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return
      "Client [name=" + name + ", cpfCnpj=" + cpfCnpj + ", contracts=" +
      contracts + ", birthDate=" + birthDate + ", telephone=" + telephone +
      ", clientType=" + clientType + "]";
  }
}
