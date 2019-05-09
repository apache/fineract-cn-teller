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

CREATE TABLE tajet_teller (
  id                           BIGSERIAL         NOT NULL,
  identifier                   VARCHAR(32)    NOT NULL,
  a_password                   VARCHAR(4096)  NOT NULL,
  a_salt                       VARCHAR(4069)  NOT NULL,
  office_identifier            VARCHAR(32)    NOT NULL,
  cashdraw_limit               NUMERIC(15, 5) NOT NULL,
  teller_account_identifier    VARCHAR(32)    NOT NULL,
  vault_account_identifier     VARCHAR(32)    NOT NULL,
  assigned_employee_identifier VARCHAR(32)    NULL,
  a_state                      VARCHAR(256)   NOT NULL,
  created_on                   TIMESTAMP(3)   NOT NULL,
  created_by                   VARCHAR(32)    NOT NULL,
  last_modified_on             TIMESTAMP(3)   NULL,
  last_modified_by             VARCHAR(32)    NULL,
  CONSTRAINT tajet_teller_pk PRIMARY KEY (id),
  CONSTRAINT tajet_teller_identifier_uq UNIQUE (identifier));

CREATE TABLE tajet_teller_transactions (
  id                          BIGSERIAL      NOT NULL,
  teller_id                   BIGINT         NOT NULL,
  identifier                  VARCHAR(32)    NOT NULL,
  transaction_type            VARCHAR(32)    NOT NULL,
  transaction_date            TIMESTAMP(3)   NOT NULL,
  customer_identifier         VARCHAR(32)    NOT NULL,
  product_identifier          VARCHAR(32)    NOT NULL,
  product_case_identifier     VARCHAR(32)    NULL,
  customer_account_identifier VARCHAR(32)    NOT NULL,
  target_account_identifier   VARCHAR(32)    NULL,
  clerk                       VARCHAR(32)    NOT NULL,
  amount                      NUMERIC(15, 5) NOT NULL,
  a_state                     VARCHAR(256)   NOT NULL,
  CONSTRAINT tajet_teller_transactions_pk PRIMARY KEY (id),
  CONSTRAINT tajet_teller_identifier_unq UNIQUE (identifier),
  CONSTRAINT tajet_teller_transaction_fk FOREIGN KEY (teller_id) REFERENCES tajet_teller (id));
