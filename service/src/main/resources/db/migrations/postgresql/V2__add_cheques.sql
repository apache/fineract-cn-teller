--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

CREATE TABLE tajet_cheques (
  id                       BIGSERIAL      NOT NULL,
  teller_transaction_id    BIGINT         NOT NULL,
  cheque_number            VARCHAR(8)     NOT NULL,
  branch_sort_code         VARCHAR(11)    NOT NULL,
  account_number           VARCHAR(34)    NOT NULL,
  drawee                   VARCHAR(2048)  NOT NULL,
  drawer                   VARCHAR(256)   NOT NULL,
  payee                    VARCHAR(256)   NOT NULL,
  amount                   NUMERIC(15, 5) NOT NULL,
  date_issued              DATE           NOT NULL,
  open_cheque              BOOLEAN        NULL,
  CONSTRAINT tajet_cheques_pk PRIMARY KEY (id),
  CONSTRAINT tajet_cheques_uq UNIQUE (cheque_number, branch_sort_code, account_number),
  CONSTRAINT tajet_cheques_teller_tx_fk FOREIGN KEY (teller_transaction_id) REFERENCES tajet_teller_transactions (id));