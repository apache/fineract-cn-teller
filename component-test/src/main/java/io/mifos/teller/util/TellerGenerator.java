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
package io.mifos.teller.util;

import io.mifos.teller.api.v1.domain.Teller;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;

public class TellerGenerator {

  public TellerGenerator() {
    super();
  }

  public static Teller createRandomTeller() {
    final Teller teller = new Teller();
    teller.setCode(RandomStringUtils.randomAlphanumeric(32));
    teller.setPassword(RandomStringUtils.randomAlphanumeric(12));
    teller.setTellerAccountIdentifier(RandomStringUtils.randomAlphanumeric(34));
    teller.setVaultAccountIdentifier(RandomStringUtils.randomAlphanumeric(34));
    teller.setChequesReceivableAccount(RandomStringUtils.randomAlphanumeric(34));
    teller.setCashOverShortAccount(RandomStringUtils.randomAlphanumeric(34));
    teller.setCashdrawLimit(BigDecimal.valueOf(10000L));

    return teller;
  }
}
