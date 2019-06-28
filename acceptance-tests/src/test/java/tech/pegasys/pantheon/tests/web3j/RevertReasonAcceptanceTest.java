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
package tech.pegasys.pantheon.tests.web3j;

import static tech.pegasys.pantheon.tests.acceptance.dsl.ContractUtils.sendWithRevert;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;
import tech.pegasys.pantheon.tests.web3j.generated.RevertReason;

import org.junit.Before;
import org.junit.Test;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

public class RevertReasonAcceptanceTest extends AcceptanceTestBase {

  private PantheonNode minerNode;

  @Before
  public void setUp() throws Exception {
    minerNode = pantheon.createMinerNode("miner-node-withRevertReason");
    cluster.start(minerNode);
  }

  @Test
  public void mustRevertWithRevertReason() throws Exception {

    final RevertReason revertReasonContract =
        minerNode.execute(contractTransactions.createSmartContract(RevertReason.class));

    final EthSendTransaction ethSendTransaction =
        sendWithRevert(
            RevertReason.FUNC_REVERTWITHREVERTREASON,
            revertReasonContract.getContractAddress(),
            minerNode.web3jService());

    minerNode.verify(
        eth.expectSuccessfulTransactionReceiptWithReason(
            ethSendTransaction.getTransactionHash(),
            minerNode.jsonRpcListenHost(),
            minerNode.getJsonRpcHttpPort()));
  }

  @Test
  public void mustRevertWithoutRevertReason() throws Exception {

    final RevertReason revertReasonContract =
        minerNode.execute(contractTransactions.createSmartContract(RevertReason.class));

    final EthSendTransaction ethSendTransaction =
        sendWithRevert(
            RevertReason.FUNC_REVERTWITHOUTREVERTREASON,
            revertReasonContract.getContractAddress(),
            minerNode.web3jService());

    minerNode.verify(
        eth.expectSuccessfulTransactionReceiptWithoutReason(
            ethSendTransaction.getTransactionHash(),
            minerNode.jsonRpcListenHost(),
            minerNode.getJsonRpcHttpPort()));
  }
}
