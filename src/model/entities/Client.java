package model.entities;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import model.entities.Guarantee.GuaranteeType;

public class Client {
  private int id;
  private String name;
  private String cpfCnpj;
  private LocalDate birthDate;
  private List<String> contracts;
  private String telephone;
  private GuaranteeType guarantee;
  private List<String> guarantorName;
  private double deposit;
  private ClientType clientType;

  public enum ClientType {
    TENANT,
    LANDLORD;

    @Override
    public String toString() {
      switch (this) {
        case TENANT:
          return "Locatário";
        case LANDLORD:
          return "Locador";
        default:
          return super.toString(); // Fallback to the default name() if needed
      }
    }
  }

  public Client() {}

  public Client(
    int id, String name, String cpfCnpj, LocalDate birthDate,
    List<String> contracts, String telephone, GuaranteeType guarantee,
    List<String> guarantorName, double deposit, ClientType clientType
  ) {
    this.id = id;
    this.name = name;
    this.cpfCnpj = cpfCnpj;
    this.birthDate = birthDate;
    this.contracts = contracts;
    this.telephone = telephone;
    this.guarantee = guarantee;
    this.guarantorName = guarantorName;
    this.deposit = deposit;
    this.clientType = (clientType != null) ? clientType : ClientType.TENANT;
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

  public List<String> getGuarantorName() {
    return guarantorName;
  }

  public void setGuarantorName(List<String> guarantorName) {
    this.guarantorName = guarantorName;
  }

  public GuaranteeType getGuarantee() {
    return guarantee;
  }

  public void setGuarantee(GuaranteeType guarantee) {
    this.guarantee = guarantee;
  }

  public double getDeposit() {
    return deposit;
  }

  public void setDeposit(double deposit) {
    this.deposit = deposit;
  }

  public ClientType getClientType() {
    return clientType;
  }

  public void setClientType(ClientType clientType) {
    this.clientType = clientType;
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
      "Client [name=" + name + ", cpfCnpj=" + cpfCnpj +
      ", birthDate=" + birthDate + ", contracts=" + contracts +
      ", telephone=" + telephone + ", guarantorName=" + guarantorName +
      ", guarantee=" + guarantee + ", clientType=" + clientType + "]";
  }
}
