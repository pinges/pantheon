package net.consensys.pantheon.ethereum.p2p.discovery.internal;

import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;
import static java.util.Collections.unmodifiableList;

import net.consensys.pantheon.ethereum.p2p.discovery.DiscoveryPeer;
import net.consensys.pantheon.ethereum.p2p.peers.PeerId;
import net.consensys.pantheon.util.bytes.BytesValue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * As peers are discovered on the network, they are added to one of the k-buckets described by this
 * class. All peers encountered will be subject for inclusion in this data structure.
 *
 * <p>This implementation is driven by an array sorted by access time, where the head is the <i>most
 * recently accessed peer</i> and the tail is the <i>least recently accessed peer</i>. If the bucket
 * is full, the <i>least recently accessed peer</i> is proposed for eviction, thus aiming to keep
 * the bucket filled with alive, responsive peers.
 */
public class Bucket {
  private final DiscoveryPeer[] kBucket;
  private final int bucketSize;
  private int tailIndex = -1;

  /**
   * Creates a new bucket with the provided maximum size.
   *
   * @param bucketSize every k-bucket maintains a constituent list having up to bucketSize entries,
   *     default is 16.
   */
  Bucket(final int bucketSize) {
    this.bucketSize = bucketSize;
    this.kBucket = new DiscoveryPeer[bucketSize];
  }

  /**
   * Returns the peer with the provided ID if it exists in the bucket.
   *
   * <p>This operation presupposes that the system has been in recent contact with this peer, hence
   * it relocates it to to the head of the list.
   *
   * @param id The peer's ID (public key).
   * @return An empty optional if the peer was not a member of this bucket, or a filled optional if
   *     it was.
   */
  synchronized Optional<DiscoveryPeer> getAndTouch(final BytesValue id) {
    for (int i = 0; i <= tailIndex; i++) {
      final DiscoveryPeer p = kBucket[i];
      if (id.equals(p.getId())) {
        arraycopy(kBucket, 0, kBucket, 1, i);
        kBucket[0] = p;
        return Optional.of(p);
      }
    }
    return Optional.empty();
  }

  /**
   * Appends the specified element to the head of the bucket array if capacity hasn't yet been
   * reached. Shifts the element currently at that position (if any) and any subsequent elements to
   * the right (adds one to their indices). If the bucket is empty, the last argument (length to
   * copy) will be 0. This method will not "touch" the peer, i.e. relocate it to the head.
   *
   * <p>In the case that the bucket is at maximum capacity the peer at the tail of the list,
   * necessarily the peer that has been incomunicative for the longest time is returned as a
   * potential eviction candidate.
   *
   * @param peer element to be appended to this list
   * @return an empty optional or alternatively the least recently contacted peer (tail of array)
   * @throws IllegalArgumentException The peer already existed in the bucket.
   */
  synchronized Optional<DiscoveryPeer> add(final DiscoveryPeer peer)
      throws IllegalArgumentException {
    assert tailIndex >= -1 && tailIndex < bucketSize;

    // Avoid duplicating the peer if it already exists in the bucket.
    for (int i = 0; i <= tailIndex; i++) {
      if (peer.equals(kBucket[i])) {
        throw new IllegalArgumentException(
            String.format("Tried to add duplicate peer to k-bucket: %s", peer.getId()));
      }
    }
    if (tailIndex == bucketSize - 1) {
      return Optional.of(kBucket[tailIndex]);
    }
    arraycopy(kBucket, 0, kBucket, 1, ++tailIndex);
    kBucket[0] = peer;
    return Optional.empty();
  }

  /**
   * Removes the element at the specified position in this list. Shifts any subsequent elements to
   * the left (subtracts one from their indices).
   *
   * @param peer the element to be removed
   * @return <tt>true</tt>
   */
  synchronized boolean evict(final PeerId peer) {
    // If the bucket is empty, there's nothing to evict.
    if (tailIndex < 0) {
      return false;
    }
    // If found, shift all subsequent elements to the left, and decrement tailIndex.
    for (int i = 0; i <= tailIndex; i++) {
      if (peer.equals(kBucket[i])) {
        arraycopy(kBucket, i + 1, kBucket, i, tailIndex - i);
        kBucket[tailIndex--] = null;
        return true;
      }
    }
    return false;
  }

  /**
   * Returns an immutable list backed by the k-bucket array. This method provides a convenient way
   * to access all peers maintained by the instance of Bucket under consideration.
   *
   * @return immutable view of the peer array
   */
  synchronized List<DiscoveryPeer> peers() {
    return unmodifiableList(asList(copyOf(kBucket, tailIndex + 1)));
  }

  @Override
  public String toString() {
    return Arrays.toString(kBucket);
  }
}