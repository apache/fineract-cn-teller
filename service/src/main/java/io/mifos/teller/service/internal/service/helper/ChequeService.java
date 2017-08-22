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
package io.mifos.teller.service.internal.service.helper;

import io.mifos.cheque.api.v1.client.ChequeManager;
import io.mifos.cheque.api.v1.domain.ChequeTransaction;
import io.mifos.teller.ServiceConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ChequeService {

  private final Logger logger;
  private final ChequeManager chequeManager;

  @Autowired
  public ChequeService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                       final ChequeManager chequeManager) {
    super();
    this.logger = logger;
    this.chequeManager = chequeManager;
  }

  public void process(final ChequeTransaction chequeTransaction) {
    this.chequeManager.process(chequeTransaction);
  }
}
