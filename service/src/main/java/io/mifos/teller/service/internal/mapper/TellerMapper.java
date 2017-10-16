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
package io.mifos.teller.service.internal.mapper;

import io.mifos.core.lang.DateConverter;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.service.internal.repository.TellerEntity;

public class TellerMapper {

  private TellerMapper() {
    super();
  }

  public static Teller map(final TellerEntity tellerEntity) {
    final Teller teller = new Teller();
    teller.setCode(tellerEntity.getIdentifier());
    teller.setTellerAccountIdentifier(tellerEntity.getTellerAccountIdentifier());
    teller.setVaultAccountIdentifier(tellerEntity.getVaultAccountIdentifier());
    teller.setCashdrawLimit(tellerEntity.getCashdrawLimit());
    teller.setAssignedEmployee(tellerEntity.getAssignedEmployeeIdentifier());
    teller.setChequesReceivableAccount(tellerEntity.getChequesReceivableAccount());
    teller.setCashOverShortAccount(tellerEntity.getCashOverShortAccount());
    teller.setState(tellerEntity.getState());
    if (tellerEntity.getCreatedBy() != null) {
      teller.setCreatedBy(tellerEntity.getCreatedBy());
      teller.setCreatedOn(DateConverter.toIsoString(tellerEntity.getCreatedOn()));
    }
    if (tellerEntity.getLastModifiedBy() != null) {
      teller.setLastModifiedBy(tellerEntity.getLastModifiedBy());
      teller.setLastModifiedOn(DateConverter.toIsoString(tellerEntity.getLastModifiedOn()));
    }
    if (tellerEntity.getLastOpenedBy() != null) {
      teller.setLastOpenedBy(tellerEntity.getLastOpenedBy());
      teller.setLastOpenedOn(DateConverter.toIsoString(tellerEntity.getLastOpenedOn()));
    }

    return teller;
  }

  public static TellerEntity map(final String officeIdentifier, final Teller teller) {
    final TellerEntity tellerEntity = new TellerEntity();
    tellerEntity.setIdentifier(teller.getCode());
    tellerEntity.setPassword(teller.getPassword());
    tellerEntity.setOfficeIdentifier(officeIdentifier);
    tellerEntity.setTellerAccountIdentifier(teller.getTellerAccountIdentifier());
    tellerEntity.setVaultAccountIdentifier(teller.getVaultAccountIdentifier());
    tellerEntity.setChequesReceivableAccount(teller.getChequesReceivableAccount());
    tellerEntity.setCashOverShortAccount(teller.getCashOverShortAccount());
    tellerEntity.setCashdrawLimit(teller.getCashdrawLimit());
    tellerEntity.setAssignedEmployeeIdentifier(teller.getAssignedEmployee());
    if (teller.getState() != null) {
      tellerEntity.setState(teller.getState());
    }
    if (teller.getCreatedBy() != null) {
      tellerEntity.setCreatedBy(teller.getCreatedBy());
      tellerEntity.setCreatedOn(DateConverter.fromIsoString(teller.getCreatedOn()));
    }
    if (teller.getLastModifiedBy() != null) {
      tellerEntity.setLastModifiedBy(teller.getLastModifiedBy());
      tellerEntity.setLastModifiedOn(DateConverter.fromIsoString(teller.getLastModifiedOn()));
    }
    if (teller.getLastOpenedBy() != null) {
      tellerEntity.setLastOpenedBy(teller.getLastOpenedBy());
      tellerEntity.setLastOpenedOn(DateConverter.fromIsoString(teller.getLastOpenedOn()));
    }

    return tellerEntity;
  }
}
