package net.consensys.pantheon.ethereum.mainnet;

import net.consensys.pantheon.ethereum.core.Address;
import net.consensys.pantheon.ethereum.core.Gas;
import net.consensys.pantheon.ethereum.core.MutableAccount;
import net.consensys.pantheon.ethereum.core.Wei;
import net.consensys.pantheon.ethereum.vm.EVM;
import net.consensys.pantheon.ethereum.vm.MessageFrame;
import net.consensys.pantheon.util.bytes.BytesValue;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainnetMessageCallProcessor extends AbstractMessageProcessor {
  private static final Logger LOGGER = LogManager.getLogger();

  private final PrecompileContractRegistry precompiles;

  public MainnetMessageCallProcessor(
      final EVM evm,
      final PrecompileContractRegistry precompiles,
      final Collection<Address> forceCommitAddresses) {
    super(evm, forceCommitAddresses);
    this.precompiles = precompiles;
  }

  public MainnetMessageCallProcessor(final EVM evm, final PrecompileContractRegistry precompiles) {
    super(evm, ImmutableSet.of());
    this.precompiles = precompiles;
  }

  @Override
  public void start(final MessageFrame frame) {
    LOGGER.trace("Executing message-call");

    transferValue(frame);

    // Check first if the message call is to a pre-compile contract
    final PrecompiledContract precompile = precompiles.get(frame.getContractAddress());
    if (precompile != null) {
      executePrecompile(precompile, frame);
    } else {
      frame.setState(MessageFrame.State.CODE_EXECUTING);
    }
  }

  @Override
  protected void codeSuccess(final MessageFrame frame) {
    LOGGER.trace(
        "Successful message call of {} to {} (Gas remaining: {})",
        frame.getSenderAddress(),
        frame.getRecipientAddress(),
        frame.getRemainingGas());
    frame.setState(MessageFrame.State.COMPLETED_SUCCESS);
  }

  /**
   * Transfers the message call value from the sender to the recipient.
   *
   * <p>Assumes that the transaction has been validated so that the sender has the required fund as
   * of the world state of this executor.
   */
  private void transferValue(final MessageFrame frame) {
    final MutableAccount senderAccount = frame.getWorldState().getMutable(frame.getSenderAddress());
    // The yellow paper explicitly states that if the recipient account doesn't exist at this
    // point, it is created.
    final MutableAccount recipientAccount =
        frame.getWorldState().getOrCreate(frame.getRecipientAddress());

    if (frame.getRecipientAddress().equals(frame.getSenderAddress())) {
      LOGGER.trace("Message call of {} to itself: no fund transferred", frame.getSenderAddress());
    } else {
      final Wei prevSenderBalance = senderAccount.decrementBalance(frame.getValue());
      final Wei prevRecipientBalance = recipientAccount.incrementBalance(frame.getValue());

      LOGGER.trace(
          "Transferred value {} for message call from {} ({} -> {}) to {} ({} -> {})",
          frame.getValue(),
          frame.getSenderAddress(),
          prevSenderBalance,
          senderAccount.getBalance(),
          frame.getRecipientAddress(),
          prevRecipientBalance,
          recipientAccount.getBalance());
    }
  }

  /**
   * Executes this message call knowing that it is a call to the provide pre-compiled contract.
   *
   * @param contract The contract this is a message call to.
   */
  private void executePrecompile(final PrecompiledContract contract, final MessageFrame frame) {
    final Gas gasRequirement = contract.gasRequirement(frame.getInputData());
    if (frame.getRemainingGas().compareTo(gasRequirement) < 0) {
      LOGGER.trace(
          "Not enough gas available for pre-compiled contract code {}: requiring "
              + "{} but only {} gas available",
          contract,
          gasRequirement,
          frame.getRemainingGas());
      frame.setState(MessageFrame.State.EXCEPTIONAL_HALT);
    } else {
      frame.decrementRemainingGas(gasRequirement);
      final BytesValue output = contract.compute(frame.getInputData());
      if (output != null) {
        frame.setOutputData(output);
        LOGGER.trace(
            "Precompiled contract {}  successfully executed (gas consumed: {})",
            contract,
            gasRequirement);
        frame.setState(MessageFrame.State.COMPLETED_SUCCESS);
      } else {
        LOGGER.trace(
            "Precompiled contract  {} failed (gas consumed: {})", contract, gasRequirement);
        frame.setState(MessageFrame.State.EXCEPTIONAL_HALT);
      }
    }
  }
}