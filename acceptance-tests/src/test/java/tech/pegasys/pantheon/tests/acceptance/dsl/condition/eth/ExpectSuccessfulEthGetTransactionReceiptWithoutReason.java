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
package tech.pegasys.pantheon.tests.acceptance.dsl.condition.eth;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.tests.acceptance.dsl.WaitUtils;
import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth.EthGetTransactionReceiptRawResponseTransaction;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth.EthRawRequestFactory.TransactionReceiptRaw;

import java.util.Optional;

public class ExpectSuccessfulEthGetTransactionReceiptWithoutReason implements Condition {

  private final EthGetTransactionReceiptRawResponseTransaction transaction;

  public ExpectSuccessfulEthGetTransactionReceiptWithoutReason(
      final EthGetTransactionReceiptRawResponseTransaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public void verify(final Node node) {
    WaitUtils.waitFor(() -> assertThat(revertReasonIsEmpty(node)).isPresent());
  }

  private Optional<String> revertReasonIsEmpty(final Node node) {
    return node.execute(transaction)
        .map(TransactionReceiptRaw::getRevertReason)
        .filter(String::isEmpty);
  }
}
