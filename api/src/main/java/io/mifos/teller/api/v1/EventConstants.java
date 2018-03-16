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
package io.mifos.teller.api.v1;

@SuppressWarnings("unused")
public interface EventConstants {

  String DESTINATION = "teller-v1";

  String SELECTOR_NAME = "operation";

  String INITIALIZE = "initialize";
  String SELECTOR_INITIALIZE = SELECTOR_NAME + " = '" + INITIALIZE + "'";

  String POST_TELLER = "post-teller";
  String SELECTOR_POST_TELLER = SELECTOR_NAME + " = '" + POST_TELLER + "'";
  String PUT_TELLER = "put-teller";
  String SELECTOR_PUT_TELLER = SELECTOR_NAME + " = '" + PUT_TELLER + "'";
  String OPEN_TELLER = "open-teller";
  String SELECTOR_OPEN_TELLER = SELECTOR_NAME + " = '" + OPEN_TELLER + "'";
  String CLOSE_TELLER = "close-teller";
  String SELECTOR_CLOSE_TELLER = SELECTOR_NAME + " = '" + CLOSE_TELLER + "'";
  String ACTIVATE_TELLER = "activate-teller";
  String SELECTOR_ACTIVATE_TELLER = SELECTOR_NAME + " = '" + ACTIVATE_TELLER + "'";
  String PAUSE_TELLER = "pause-teller";
  String SELECTOR_PAUSE_TELLER = SELECTOR_NAME + " = '" + PAUSE_TELLER + "'";
  String DELETE_TELLER = "delete-teller";
  String SELECTOR_DELETE_TELLER = SELECTOR_NAME + " = '" + DELETE_TELLER + "'";
  String SAVE_DENOMINATION = "post-teller-denomination";
  String SELECTOR_SAVE_DENOMINATION = SELECTOR_NAME + " = '" + SAVE_DENOMINATION + "'";

  String INIT_TRANSACTION = "init-transaction";
  String SELECTOR_INIT_TRANSACTION = SELECTOR_NAME + " = '" + INIT_TRANSACTION + "'";
  String CONFIRM_TRANSACTION = "confirm-transaction";
  String SELECTOR_CONFIRM_TRANSACTION = SELECTOR_NAME + " = '" + CONFIRM_TRANSACTION + "'";
  String CANCEL_TRANSACTION = "cancel-transaction";
  String SELECTOR_CANCEL_TRANSACTION = SELECTOR_NAME + " = '" + CANCEL_TRANSACTION + "'";
  String AUTHENTICATE_TELLER = "authenticate-teller";
  String SELECTOR_AUTHENTICATE_TELLER = SELECTOR_NAME + " = '" + AUTHENTICATE_TELLER + "'";
}
