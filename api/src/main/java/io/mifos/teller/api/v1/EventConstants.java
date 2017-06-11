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
}
