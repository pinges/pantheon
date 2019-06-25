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
package tech.pegasys.pantheon.ethereum.permissioning;

import tech.pegasys.pantheon.ethereum.core.Synchronizer;
import tech.pegasys.pantheon.ethereum.p2p.peers.EnodeURL;
import tech.pegasys.pantheon.ethereum.permissioning.node.NodePermissioningController;
import tech.pegasys.pantheon.ethereum.permissioning.node.NodePermissioningProvider;
import tech.pegasys.pantheon.ethereum.permissioning.node.provider.SyncStatusNodePermissioningProvider;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class NodePermissioningControllerFactory {

  public NodePermissioningController create(
      final PermissioningConfiguration permissioningConfiguration,
      final Synchronizer synchronizer,
      final Collection<EnodeURL> fixedNodes,
      final BytesValue localNodeId,
      final TransactionSimulator transactionSimulator,
      final MetricsSystem metricsSystem) {

    Optional<SyncStatusNodePermissioningProvider> syncStatusProviderOptional;

    List<NodePermissioningProvider> providers = new ArrayList<>();
    if (permissioningConfiguration.getLocalConfig().isPresent()) {
      LocalPermissioningConfiguration localPermissioningConfiguration =
          permissioningConfiguration.getLocalConfig().get();
      if (localPermissioningConfiguration.isNodeWhitelistEnabled()) {
        NodeLocalConfigPermissioningController localProvider =
            new NodeLocalConfigPermissioningController(
                localPermissioningConfiguration,
                new ArrayList<>(fixedNodes),
                localNodeId,
                metricsSystem);
        providers.add(localProvider);
      }
    }

    if (permissioningConfiguration.getSmartContractConfig().isPresent()) {
      SmartContractPermissioningConfiguration smartContractPermissioningConfiguration =
          permissioningConfiguration.getSmartContractConfig().get();
      if (smartContractPermissioningConfiguration.isSmartContractNodeWhitelistEnabled()) {
        NodeSmartContractPermissioningController smartContractProvider =
            new NodeSmartContractPermissioningController(
                smartContractPermissioningConfiguration.getNodeSmartContractAddress(),
                transactionSimulator,
                metricsSystem);
        providers.add(smartContractProvider);
      }

      if (fixedNodes.isEmpty()) {
        syncStatusProviderOptional = Optional.empty();
      } else {
        syncStatusProviderOptional =
            Optional.of(
                new SyncStatusNodePermissioningProvider(synchronizer, fixedNodes, metricsSystem));
      }
    } else {
      syncStatusProviderOptional = Optional.empty();
    }
    return new NodePermissioningController(syncStatusProviderOptional, providers);
  }
}
