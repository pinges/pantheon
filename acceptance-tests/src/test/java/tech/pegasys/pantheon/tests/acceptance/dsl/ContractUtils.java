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
package tech.pegasys.pantheon.tests.acceptance.dsl;

import tech.pegasys.pantheon.tests.acceptance.dsl.account.Accounts;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;

@SuppressWarnings("rawtypes")
public class ContractUtils {

  private static final BigInteger GAS_PRICE = BigInteger.valueOf(1000);
  private static final BigInteger GAS_LIMIT = BigInteger.valueOf(3000000);
  private static final Credentials BENEFACTOR_ONE =
      Credentials.create(Accounts.GENESIS_ACCOUNT_ONE_PRIVATE_KEY);

  public static EthSendTransaction sendWithRevert(
      final String functionName, final String contractAddress, final Web3jService web3jService)
      throws IOException {
    final Function function =
        new Function(
            functionName, Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());

    final Web3j web3j = new JsonRpc2_0Web3j(web3jService);
    final RawTransactionManager transactionManager =
        new RawTransactionManager(web3j, BENEFACTOR_ONE);
    return transactionManager.sendTransaction(
        GAS_PRICE, GAS_LIMIT, contractAddress, FunctionEncoder.encode(function), BigInteger.ZERO);
  }
}
