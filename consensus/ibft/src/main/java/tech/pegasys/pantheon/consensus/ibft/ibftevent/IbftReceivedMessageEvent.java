/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.consensus.ibft.ibftevent;

import tech.pegasys.pantheon.consensus.ibft.ibftevent.IbftEvents.Type;
import tech.pegasys.pantheon.ethereum.p2p.api.Message;

public class IbftReceivedMessageEvent implements IbftEvent {

  private final Message message;

  public IbftReceivedMessageEvent(final Message message) {
    this.message = message;
  }

  public Message getMessage() {
    return message;
  }

  @Override
  public Type getType() {
    return Type.MESSAGE;
  }
}
