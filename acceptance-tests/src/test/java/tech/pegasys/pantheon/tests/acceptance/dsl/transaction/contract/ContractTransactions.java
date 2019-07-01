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
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.contract;

import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.CallSmartContractFunctionWithRevert;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.DeploySmartContractTransaction;

import org.web3j.tx.Contract;

public class ContractTransactions {

  public <T extends Contract> DeploySmartContractTransaction<T> createSmartContract(
      final Class<T> clazz) {
    return new DeploySmartContractTransaction<>(clazz);
  }

  public CallSmartContractFunctionWithRevert callSmartContractWithRevert(
      final String functionName, final String contractAddress) {
    return new CallSmartContractFunctionWithRevert(functionName, contractAddress);
  }
}
