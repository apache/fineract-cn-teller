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
package org.apache.fineract.cn.teller.service.internal.service;

import org.apache.fineract.cn.teller.ServiceConstants;
import org.apache.fineract.cn.teller.api.v1.domain.TellerTransaction;
import org.apache.fineract.cn.teller.service.internal.mapper.ChequeMapper;
import org.apache.fineract.cn.teller.service.internal.mapper.TellerTransactionMapper;
import org.apache.fineract.cn.teller.service.internal.repository.ChequeEntity;
import org.apache.fineract.cn.teller.service.internal.repository.ChequeRepository;
import org.apache.fineract.cn.teller.service.internal.repository.TellerEntity;
import org.apache.fineract.cn.teller.service.internal.repository.TellerRepository;
import org.apache.fineract.cn.teller.service.internal.repository.TellerTransactionEntity;
import org.apache.fineract.cn.teller.service.internal.repository.TellerTransactionRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class TellerOperationService {

  private final Logger logger;
  private final TellerRepository tellerRepository;
  private final TellerTransactionRepository tellerTransactionRepository;
  private final ChequeRepository chequeRepository;

  @Autowired
  public TellerOperationService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                final TellerRepository tellerRepository,
                                final TellerTransactionRepository tellerTransactionRepository,
                                final ChequeRepository chequeRepository) {
    super();
    this.logger = logger;
    this.tellerRepository = tellerRepository;
    this.tellerTransactionRepository = tellerTransactionRepository;
    this.chequeRepository = chequeRepository;
  }

  public Optional<TellerTransaction> getTellerTransaction(final String tellerTransactionIdentifier) {

    final Optional<TellerTransactionEntity> optionalTellerTransaction =
        this.tellerTransactionRepository.findByIdentifier(tellerTransactionIdentifier);

    return optionalTellerTransaction.map(tellerTransactionEntity -> {
      final TellerTransaction tellerTransaction = TellerTransactionMapper.map(tellerTransactionEntity);
      if (tellerTransaction.getTransactionType().equals(ServiceConstants.TX_CHEQUE)) {
        final Optional<ChequeEntity> optionalCheque =
            this.chequeRepository.findByTellerTransactionId(tellerTransactionEntity.getId());

        optionalCheque.ifPresent(chequeEntity -> tellerTransaction.setCheque(ChequeMapper.map(chequeEntity)));
      }
      return tellerTransaction;
    });
  }

  public List<TellerTransaction> fetchTellerTransactions(final String tellerCode, final String state) {
    final Optional<TellerEntity> optionalTellerEntity = this.tellerRepository.findByIdentifier(tellerCode);
    if (optionalTellerEntity.isPresent()) {
      final TellerEntity tellerEntity = optionalTellerEntity.get();
      if (state != null) {
        return this.tellerTransactionRepository.findByTellerAndStateOrderByTransactionDateAsc(tellerEntity, state)
            .stream()
            .map(tellerTransactionEntity -> {
              final TellerTransaction tellerTransaction = TellerTransactionMapper.map(tellerTransactionEntity);
              if (tellerTransaction.getTransactionType().equals(ServiceConstants.TX_CHEQUE)) {
                final Optional<ChequeEntity> optionalCheque =
                    this.chequeRepository.findByTellerTransactionId(tellerTransactionEntity.getId());

                optionalCheque.ifPresent(chequeEntity -> tellerTransaction.setCheque(ChequeMapper.map(chequeEntity)));
              }
              return tellerTransaction;
            })
            .collect(Collectors.toList());
      } else {
        return this.tellerTransactionRepository.findByTellerOrderByTransactionDateAsc(tellerEntity)
            .stream()
            .map(tellerTransactionEntity -> {
              final TellerTransaction tellerTransaction = TellerTransactionMapper.map(tellerTransactionEntity);
              if (tellerTransaction.getTransactionType().equals(ServiceConstants.TX_CHEQUE)) {
                final Optional<ChequeEntity> optionalCheque =
                    this.chequeRepository.findByTellerTransactionId(tellerTransactionEntity.getId());

                optionalCheque.ifPresent(chequeEntity -> tellerTransaction.setCheque(ChequeMapper.map(chequeEntity)));
              }
              return tellerTransaction;
            })
            .collect(Collectors.toList());
      }
    } else {
      throw ServiceException.notFound("Teller {0} not found.", tellerCode);
    }
  }
}
