package net.consensys.pantheon.ethereum.worldstate;

import net.consensys.pantheon.ethereum.core.AbstractWorldUpdater;
import net.consensys.pantheon.ethereum.core.Account;
import net.consensys.pantheon.ethereum.core.Address;
import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.ethereum.core.MutableWorldState;
import net.consensys.pantheon.ethereum.core.Wei;
import net.consensys.pantheon.ethereum.core.WorldState;
import net.consensys.pantheon.ethereum.core.WorldUpdater;
import net.consensys.pantheon.ethereum.rlp.RLP;
import net.consensys.pantheon.ethereum.rlp.RLPException;
import net.consensys.pantheon.ethereum.rlp.RLPInput;
import net.consensys.pantheon.ethereum.trie.MerklePatriciaTrie;
import net.consensys.pantheon.ethereum.trie.StoredMerklePatriciaTrie;
import net.consensys.pantheon.util.bytes.Bytes32;
import net.consensys.pantheon.util.bytes.BytesValue;
import net.consensys.pantheon.util.uint.UInt256;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class DefaultMutableWorldState implements MutableWorldState {

  private final MerklePatriciaTrie<Bytes32, BytesValue> accountStateTrie;
  private final Map<Address, MerklePatriciaTrie<Bytes32, BytesValue>> updatedStorageTries =
      new HashMap<>();
  private final Map<Address, BytesValue> updatedAccountCode = new HashMap<>();
  private final WorldStateStorage worldStateStorage;

  public DefaultMutableWorldState(final WorldStateStorage storage) {
    this(MerklePatriciaTrie.EMPTY_TRIE_ROOT_HASH, storage);
  }

  public DefaultMutableWorldState(
      final Bytes32 rootHash, final WorldStateStorage worldStateStorage) {
    this.worldStateStorage = worldStateStorage;
    this.accountStateTrie = newAccountStateTrie(rootHash);
  }

  public DefaultMutableWorldState(final WorldState worldState) {
    // TODO: this is an abstraction leak (and kind of incorrect in that we reuse the underlying
    // storage), but the reason for this is that the accounts() method is unimplemented below and
    // can't be until NC-754.
    if (!(worldState instanceof DefaultMutableWorldState)) {
      throw new UnsupportedOperationException();
    }

    final DefaultMutableWorldState other = (DefaultMutableWorldState) worldState;
    this.worldStateStorage = other.worldStateStorage;
    this.accountStateTrie = newAccountStateTrie(other.accountStateTrie.getRootHash());
  }

  private MerklePatriciaTrie<Bytes32, BytesValue> newAccountStateTrie(final Bytes32 rootHash) {
    return new StoredMerklePatriciaTrie<>(
        worldStateStorage::getAccountStateTrieNode, rootHash, b -> b, b -> b);
  }

  private MerklePatriciaTrie<Bytes32, BytesValue> newAccountStorageTrie(final Bytes32 rootHash) {
    return new StoredMerklePatriciaTrie<>(
        worldStateStorage::getAccountStorageTrieNode, rootHash, b -> b, b -> b);
  }

  @Override
  public Hash rootHash() {
    return Hash.wrap(accountStateTrie.getRootHash());
  }

  @Override
  public MutableWorldState copy() {
    return new DefaultMutableWorldState(rootHash(), worldStateStorage);
  }

  @Override
  public Account get(final Address address) {
    final Hash addressHash = Hash.hash(address);
    return accountStateTrie
        .get(Hash.hash(address))
        .map(bytes -> deserializeAccount(address, addressHash, bytes))
        .orElse(null);
  }

  private AccountState deserializeAccount(
      final Address address, final Hash addressHash, final BytesValue encoded) throws RLPException {
    final RLPInput in = RLP.input(encoded);
    in.enterList();

    final long nonce = in.readLongScalar();
    final Wei balance = in.readUInt256Scalar(Wei::wrap);
    final Hash storageRoot = Hash.wrap(in.readBytes32());
    final Hash codeHash = Hash.wrap(in.readBytes32());

    in.leaveList();

    return new AccountState(address, addressHash, nonce, balance, storageRoot, codeHash);
  }

  private static BytesValue serializeAccount(
      final long nonce, final Wei balance, final Hash codeHash, final Hash storageRoot) {
    return RLP.encode(
        out -> {
          out.startList();

          out.writeLongScalar(nonce);
          out.writeUInt256Scalar(balance);
          out.writeBytesValue(storageRoot);
          out.writeBytesValue(codeHash);

          out.endList();
        });
  }

  @Override
  public WorldUpdater updater() {
    return new Updater(this);
  }

  @Override
  public Stream<Account> accounts() {
    // TODO: the current trie implementation doesn't have walking capability yet (pending NC-746)
    // so this can't be implemented.
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(rootHash());
  }

  @Override
  public final boolean equals(final Object other) {
    if (!(other instanceof DefaultMutableWorldState)) {
      return false;
    }

    final DefaultMutableWorldState that = (DefaultMutableWorldState) other;
    return this.rootHash().equals(that.rootHash());
  }

  @Override
  public void persist() {
    final WorldStateStorage.Updater updater = worldStateStorage.updater();
    // Store updated code
    for (final BytesValue code : updatedAccountCode.values()) {
      updater.putCode(code);
    }
    // Commit account storage tries
    for (final MerklePatriciaTrie<Bytes32, BytesValue> updatedStorage :
        updatedStorageTries.values()) {
      updatedStorage.commit(updater::putAccountStorageTrieNode);
    }
    // Commit account updates
    accountStateTrie.commit(updater::putAccountStateTrieNode);

    // Clear pending changes that we just flushed
    updatedStorageTries.clear();
    updatedAccountCode.clear();

    // Push changes to underlying storage
    updater.commit();
  }

  // An immutable class that represents an individual account as stored in
  // in the world state's underlying merkle patricia trie.
  protected class AccountState implements Account {

    private final Address address;
    private final Hash addressHash;

    private final long nonce;
    private final Wei balance;
    private final Hash storageRoot;
    private final Hash codeHash;

    // Lazily initialized since we don't always access storage.
    private volatile MerklePatriciaTrie<Bytes32, BytesValue> storageTrie;

    private AccountState(
        final Address address,
        final Hash addressHash,
        final long nonce,
        final Wei balance,
        final Hash storageRoot,
        final Hash codeHash) {

      this.address = address;
      this.addressHash = addressHash;
      this.nonce = nonce;
      this.balance = balance;
      this.storageRoot = storageRoot;
      this.codeHash = codeHash;
    }

    private MerklePatriciaTrie<Bytes32, BytesValue> storageTrie() {
      final MerklePatriciaTrie<Bytes32, BytesValue> updatedTrie = updatedStorageTries.get(address);
      if (updatedTrie != null) {
        storageTrie = updatedTrie;
      }
      if (storageTrie == null) {
        storageTrie = newAccountStorageTrie(storageRoot);
      }
      return storageTrie;
    }

    @Override
    public Address getAddress() {
      return address;
    }

    @Override
    public Hash getAddressHash() {
      return addressHash;
    }

    @Override
    public long getNonce() {
      return nonce;
    }

    @Override
    public Wei getBalance() {
      return balance;
    }

    @Override
    public BytesValue getCode() {
      final BytesValue updatedCode = updatedAccountCode.get(address);
      if (updatedCode != null) {
        return updatedCode;
      }
      // No code is common, save the KV-store lookup.
      if (codeHash.equals(Hash.EMPTY)) {
        return BytesValue.EMPTY;
      }
      return worldStateStorage.getCode(codeHash).orElse(BytesValue.EMPTY);
    }

    @Override
    public boolean hasCode() {
      return !getCode().isEmpty();
    }

    @Override
    public UInt256 getStorageValue(final UInt256 key) {
      final Optional<BytesValue> val = storageTrie().get(Hash.hash(key.getBytes()));
      if (!val.isPresent()) {
        return UInt256.ZERO;
      }
      return convertToUInt256(val.get());
    }

    @Override
    public NavigableMap<Bytes32, UInt256> storageEntriesFrom(
        final Bytes32 startKeyHash, final int limit) {
      final NavigableMap<Bytes32, UInt256> storageEntries = new TreeMap<>();
      storageTrie()
          .entriesFrom(startKeyHash, limit)
          .forEach((key, value) -> storageEntries.put(key, convertToUInt256(value)));
      return storageEntries;
    }

    private UInt256 convertToUInt256(final BytesValue value) {
      // TODO: we could probably have an optimized method to decode a single scalar since it's used
      // pretty often.
      final RLPInput in = RLP.input(value);
      return in.readUInt256Scalar();
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("AccountState").append("{");
      builder.append("address=").append(getAddress()).append(", ");
      builder.append("nonce=").append(getNonce()).append(", ");
      builder.append("balance=").append(getBalance()).append(", ");
      builder.append("storageRoot=").append(storageRoot).append(", ");
      builder.append("codeHash=").append(codeHash);
      return builder.append("}").toString();
    }
  }

  protected static class Updater
      extends AbstractWorldUpdater<DefaultMutableWorldState, AccountState> {

    protected Updater(final DefaultMutableWorldState world) {
      super(world);
    }

    @Override
    protected AccountState getForMutation(final Address address) {
      final DefaultMutableWorldState wrapped = wrappedWorldView();
      final Hash addressHash = Hash.hash(address);
      return wrapped
          .accountStateTrie
          .get(addressHash)
          .map(bytes -> wrapped.deserializeAccount(address, addressHash, bytes))
          .orElse(null);
    }

    @Override
    public Collection<Account> getTouchedAccounts() {
      return new ArrayList<>(updatedAccounts());
    }

    @Override
    public void revert() {
      deletedAccounts().clear();
      updatedAccounts().clear();
    }

    @Override
    public void commit() {
      final DefaultMutableWorldState wrapped = wrappedWorldView();

      for (final Address address : deletedAccounts()) {
        final Hash addressHash = Hash.hash(address);
        wrapped.accountStateTrie.remove(addressHash);
        wrapped.updatedStorageTries.remove(address);
        wrapped.updatedAccountCode.remove(address);
      }

      // TODO: this is inefficient with a real persistent world state as doing updates one by one
      // might create a lot of garbage nodes that will be persisted without being needed. Also, if
      // the state is big, doing update one by one is not algorithmically optimal in general. We
      // should create a small in-memory trie of the updates first, and then apply this in bulk
      // to the real world state as a merge of trie.
      for (final UpdateTrackingAccount<AccountState> updated : updatedAccounts()) {
        final AccountState origin = updated.getWrappedAccount();

        // Save the code in key-value storage ...
        Hash codeHash = origin == null ? Hash.EMPTY : origin.codeHash;
        if (updated.codeWasUpdated()) {
          codeHash = Hash.hash(updated.getCode());
          wrapped.updatedAccountCode.put(updated.getAddress(), updated.getCode());
        }
        // ...and storage in the account trie first.
        // TODO: same remark as above really, this could be make more efficient by "batching"
        final SortedMap<UInt256, UInt256> updatedStorage = updated.getUpdatedStorage();
        Hash storageRoot;
        MerklePatriciaTrie<Bytes32, BytesValue> storageTrie;
        if (updatedStorage.isEmpty()) {
          storageRoot = origin == null ? Hash.EMPTY_TRIE_HASH : origin.storageRoot;
        } else {
          storageTrie =
              origin == null
                  ? wrapped.newAccountStorageTrie(Hash.EMPTY_TRIE_HASH)
                  : origin.storageTrie();
          wrapped.updatedStorageTries.put(updated.getAddress(), storageTrie);
          for (final Map.Entry<UInt256, UInt256> entry : updatedStorage.entrySet()) {
            final UInt256 value = entry.getValue();
            final Hash keyHash = Hash.hash(entry.getKey().getBytes());
            if (value.isZero()) {
              storageTrie.remove(keyHash);
            } else {
              storageTrie.put(keyHash, RLP.encode(out -> out.writeUInt256Scalar(entry.getValue())));
            }
          }
          storageRoot = Hash.wrap(storageTrie.getRootHash());
        }

        // Lastly, save the new account.
        final BytesValue account =
            serializeAccount(updated.getNonce(), updated.getBalance(), codeHash, storageRoot);

        wrapped.accountStateTrie.put(updated.getAddressHash(), account);
      }
    }
  }
}