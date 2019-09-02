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

ALTER TABLE tajet_teller ADD cash_over_short_account VARCHAR(34) NULL;
ALTER TABLE tajet_teller ADD denomination_required BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE tajet_teller_denominations (
  id                      BIGSERIAL     NOT NULL,
  teller_id               BIGINT        NOT NULL,
  counted_total           NUMERIC(15,5) NOT NULL,
  note                    VARCHAR(512)  NULL,
  adjusting_journal_entry VARCHAR(32)   NULL,
  created_on              TIMESTAMP(3)  NOT NULL,
  created_by              VARCHAR(32)   NOT NULL,
  CONSTRAINT tajet_teller_denominations_pk PRIMARY KEY (id),
  CONSTRAINT tajet_teller_denominations_fk FOREIGN KEY (teller_id) REFERENCES tajet_teller (id));