package io.mifos.teller.domain;

public class TellerAuthentication {

  private String employeeIdentifier;
  private String password;

  public TellerAuthentication() {
    super();
  }

  public String getEmployeeIdentifier() {
    return this.employeeIdentifier;
  }

  public void setEmployeeIdentifier(final String employeeIdentifier) {
    this.employeeIdentifier = employeeIdentifier;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }
}
