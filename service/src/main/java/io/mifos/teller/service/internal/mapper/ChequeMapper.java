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
import io.mifos.teller.api.v1.domain.Cheque;
import io.mifos.teller.api.v1.domain.MICR;
import io.mifos.teller.service.internal.repository.ChequeEntity;

public class ChequeMapper {
  private ChequeMapper() {
    super();
  }

  public static io.mifos.cheque.api.v1.domain.Cheque map(final Cheque tellerCheque) {
    final io.mifos.cheque.api.v1.domain.Cheque cheque = new io.mifos.cheque.api.v1.domain.Cheque();

    final MICR tellerChequeMicr = tellerCheque.getMicr();
    final io.mifos.cheque.api.v1.domain.MICR micr = new io.mifos.cheque.api.v1.domain.MICR();
    micr.setChequeNumber(tellerChequeMicr.getChequeNumber());
    micr.setBranchSortCode(tellerChequeMicr.getBranchSortCode());
    micr.setAccountNumber(tellerChequeMicr.getAccountNumber());
    cheque.setMicr(micr);

    cheque.setDrawee(tellerCheque.getDrawee());
    cheque.setDrawer(tellerCheque.getDrawer());
    cheque.setPayee(tellerCheque.getPayee());
    cheque.setDateIssued(tellerCheque.getDateIssued());
    cheque.setAmount(tellerCheque.getAmount());
    cheque.setOpenCheque(tellerCheque.isOpenCheque());

    return cheque;
  }

  public static Cheque map(final ChequeEntity chequeEntity) {
    final MICR micr = new MICR();
    micr.setChequeNumber(chequeEntity.getChequeNumber());
    micr.setBranchSortCode(chequeEntity.getBranchSortCode());
    micr.setAccountNumber(chequeEntity.getAccountNumber());

    final Cheque cheque = new Cheque();
    cheque.setMicr(micr);
    cheque.setDrawee(chequeEntity.getDrawee());
    cheque.setDrawer(chequeEntity.getDrawer());
    cheque.setPayee(chequeEntity.getPayee());
    cheque.setDateIssued(DateConverter.toIsoString(chequeEntity.getDateIssued().toLocalDate()));
    cheque.setAmount(chequeEntity.getAmount().toString());
    cheque.setOpenCheque(chequeEntity.getOpenCheque());
    return cheque;
  }
}
