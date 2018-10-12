package net.consensys.pantheon.tests.acceptance;

import static net.consensys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonMinerNode;
import static net.consensys.pantheon.tests.acceptance.dsl.node.PantheonNodeConfig.pantheonNode;
import static org.web3j.utils.Convert.Unit.ETHER;

import net.consensys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import net.consensys.pantheon.tests.acceptance.dsl.account.Account;
import net.consensys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import org.junit.Before;
import org.junit.Test;

public class CreateAccountAcceptanceTest extends AcceptanceTestBase {

  private PantheonNode minerNode;
  private PantheonNode fullNode;

  @Before
  public void setUp() throws Exception {
    minerNode = cluster.create(pantheonMinerNode("node1"));
    fullNode = cluster.create(pantheonNode("node2"));
    cluster.start(minerNode, fullNode);
  }

  @Test
  public void shouldCreateAnAccount() {
    final Account account = accounts.createAccount("account1", "20", ETHER, fullNode);
    accounts.waitForAccountBalance(account, "20", ETHER, minerNode);
    accounts.waitForAccountBalance(account, "20", ETHER, fullNode);
  }
}