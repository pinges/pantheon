/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth;

import java.util.Arrays;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class EthRawRequestFactory {

  public static class TransactionReceiptRaw extends TransactionReceipt {
    private String revertReason;

    public TransactionReceiptRaw() {}

    public void setRevertReason(final String revertReason) {
      this.revertReason = revertReason;
    }

    public String getRevertReason() {
      return revertReason;
    }
  }

  public static class EthGetTransactionReceiptRawResponse extends Response<TransactionReceiptRaw> {}

  private final Web3jService web3jService;

  public EthRawRequestFactory(final Web3jService web3jService) {
    this.web3jService = web3jService;
  }

  public Request<?, EthGetTransactionReceiptRawResponse> ethGetTransactionReceiptRaw(
      String transactionHash) {
    return new Request<>(
        "eth_getTransactionReceipt",
        Arrays.asList(transactionHash),
        web3jService,
        EthGetTransactionReceiptRawResponse.class);
  }
}
