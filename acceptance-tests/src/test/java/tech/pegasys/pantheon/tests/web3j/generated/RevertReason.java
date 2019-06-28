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
package tech.pegasys.pantheon.tests.web3j.generated;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * Auto generated code.
 *
 * <p><strong>Do not modify!</strong>
 *
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the <a
 * href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.3.0.
 */
@SuppressWarnings("rawtypes")
public class RevertReason extends Contract {
  private static final String BINARY =
      "60806040526000805534801561001457600080fd5b5060de806100236000396000f3fe6080604052348015600f57600080fd5b5060043610603c5760003560e01c806311f95f6f1460415780633fa4f245146049578063ff489d31146061575b600080fd5b60476067565b005b604f60a3565b60408051918252519081900360200190f35b6047603c565b6040805162461bcd60e51b815260206004820152600c60248201526b2932bb32b93a2932b0b9b7b760a11b604482015290519081900360640190fd5b6000548156fea265627a7a72305820344bbe39744359c54d48fe55c3f74fd985bb207dd59e3b0e041c55ce0ec876cb64736f6c63430005090032";

  public static final String FUNC_REVERTWITHREVERTREASON = "revertWithRevertReason";

  public static final String FUNC_VALUE = "value";

  public static final String FUNC_REVERTWITHOUTREVERTREASON = "revertWithoutRevertReason";

  @Deprecated
  protected RevertReason(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  protected RevertReason(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
  }

  @Deprecated
  protected RevertReason(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  protected RevertReason(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public RemoteCall<TransactionReceipt> revertWithRevertReason() {
    final Function function =
        new Function(
            FUNC_REVERTWITHREVERTREASON,
            Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteCall<BigInteger> value() {
    final Function function =
        new Function(
            FUNC_VALUE,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteCall<TransactionReceipt> revertWithoutRevertReason() {
    final Function function =
        new Function(
            FUNC_REVERTWITHOUTREVERTREASON,
            Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  @Deprecated
  public static RevertReason load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new RevertReason(contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  @Deprecated
  public static RevertReason load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new RevertReason(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  public static RevertReason load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    return new RevertReason(contractAddress, web3j, credentials, contractGasProvider);
  }

  public static RevertReason load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    return new RevertReason(contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public static RemoteCall<RevertReason> deploy(
      Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
    return deployRemoteCall(
        RevertReason.class, web3j, credentials, contractGasProvider, BINARY, "");
  }

  @Deprecated
  public static RemoteCall<RevertReason> deploy(
      Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
    return deployRemoteCall(RevertReason.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
  }

  public static RemoteCall<RevertReason> deploy(
      Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
    return deployRemoteCall(
        RevertReason.class, web3j, transactionManager, contractGasProvider, BINARY, "");
  }

  @Deprecated
  public static RemoteCall<RevertReason> deploy(
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return deployRemoteCall(
        RevertReason.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
  }
}
