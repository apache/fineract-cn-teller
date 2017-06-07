package io.mifos.teller.domain;

public class TellerAuthentication {

  private String employeeIdentifier;
  private byte[] password;

  public TellerAuthentication() {
    super();
  }

  public String getEmployeeIdentifier() {
    return this.employeeIdentifier;
  }

  public void setEmployeeIdentifier(final String employeeIdentifier) {
    this.employeeIdentifier = employeeIdentifier;
  }

  public byte[] getPassword() {
    return this.password;
  }

  public void setPassword(final byte[] password) {
    this.password = password;
  }
}
