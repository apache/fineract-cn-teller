/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.teller.api.v1.domain;

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
