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
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth;

import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.NodeRequests;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;

public class EthGetTransactionReceiptRawResponseTransaction implements Transaction<String> {

  private static final String EXCEPTION_WHEN_RETRIEVING_RECEIPT =
      "Exception when retrieving receipt.";
  private static final String TIMEOUT_WHILE_RETRIEVING_RECEIPT =
      "Timeout while retrieving receipt.";
  private final String transactionHash;
  private final String host;
  private final Integer jsonRpcHttpSocketPort;

  public EthGetTransactionReceiptRawResponseTransaction(
      final String transactioHash,
      final Optional<String> host,
      final Optional<Integer> jsonRpcHttpSocketPort) {
    this.transactionHash = transactioHash;
    this.host = host.orElse("localhost");
    this.jsonRpcHttpSocketPort = jsonRpcHttpSocketPort.orElse(8545);
  }

  @Override
  public String execute(final NodeRequests node) {
    final Vertx vertx = Vertx.vertx();
    final CompletableFuture<String> future = new CompletableFuture<>();
    final HttpClient httpClient = vertx.createHttpClient();
    final HttpClientRequest request =
        httpClient.request(
            HttpMethod.POST,
            jsonRpcHttpSocketPort,
            host,
            "/",
            rh ->
                rh.bodyHandler(
                    bh -> {
                      if (rh.statusCode() == 200) {
                        future.complete(bh.toString());
                      } else {
                        future.completeExceptionally(
                            new Exception(
                                "Pantheon responded with status code {" + rh.statusCode() + "}"));
                      }
                    }));
    request.setChunked(false);
    final String body =
        "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionReceipt\",\"params\" :[\""
            + transactionHash
            + "\"], \"id\":1}";
    request.end(body);
    final String response = getResponse(future);
    vertx.close();
    return response;
  }

  private String getResponse(final CompletableFuture<String> future) {
    final String response;
    try {
      response = future.get(Integer.valueOf(10), TimeUnit.SECONDS);
    } catch (final InterruptedException | ExecutionException e) {
      throw new RuntimeException(EXCEPTION_WHEN_RETRIEVING_RECEIPT, e);
    } catch (final TimeoutException e) {
      throw new RuntimeException(TIMEOUT_WHILE_RETRIEVING_RECEIPT, e);
    }
    return response;
  }
}
