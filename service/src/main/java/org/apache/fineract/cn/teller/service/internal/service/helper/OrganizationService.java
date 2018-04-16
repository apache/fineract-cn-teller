/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.teller.service.internal.service.helper;

import org.apache.fineract.cn.teller.ServiceConstants;
import org.apache.fineract.cn.office.api.v1.client.NotFoundException;
import org.apache.fineract.cn.office.api.v1.client.OrganizationManager;
import org.apache.fineract.cn.office.api.v1.domain.ExternalReference;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

  private final Logger logger;
  private final OrganizationManager organizationManager;

  @Autowired
  public OrganizationService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                             final OrganizationManager organizationManager) {
    super();
    this.logger = logger;
    this.organizationManager = organizationManager;
  }

  public boolean officeExists(final String officeIdentifier) {
    try {
      this.organizationManager.findOfficeByIdentifier(officeIdentifier);
      return true;
    } catch (final NotFoundException nfex) {
      this.logger.warn("Office {} not found.", officeIdentifier);
      return false;
    }
  }

  public boolean employeeExists(final String employeeIdentifier) {
    try {
      this.organizationManager.findEmployee(employeeIdentifier);
      return true;
    } catch (final NotFoundException nfex) {
      this.logger.warn("Employee {} not found.", employeeIdentifier);
      return false;
    }
  }

  public void setTellerReference(final String officeIdentifier) {
    final ExternalReference externalReference = new ExternalReference();
    externalReference.setType("fineract.teller");
    externalReference.setState(ExternalReference.State.ACTIVE.name());
    this.organizationManager.addExternalReference(officeIdentifier, externalReference);
  }
}
