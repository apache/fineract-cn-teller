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
package io.mifos.teller.service.internal.command;

import io.mifos.teller.api.v1.domain.TellerAuthentication;

public class AuthenticateTellerCommand {
  private final String tellerCode;
  private final TellerAuthentication tellerAuthentication;

  public AuthenticateTellerCommand(final String tellerCode, final TellerAuthentication tellerAuthentication) {
    super();
    this.tellerCode = tellerCode;
    this.tellerAuthentication = tellerAuthentication;
  }

  public String tellerCode() {
    return this.tellerCode;
  }

  public TellerAuthentication tellerAuthentication() {
    return this.tellerAuthentication;
  }
}
