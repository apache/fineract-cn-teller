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

import io.mifos.portfolio.api.v1.client.PortfolioManager;
import io.mifos.teller.ServiceConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {

  private final Logger logger;
  private final PortfolioManager portfolioManager;

  @Autowired
  public PortfolioService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                          final PortfolioManager portfolioManager) {
    super();
    this.logger = logger;
    this.portfolioManager = portfolioManager;
  }


}
