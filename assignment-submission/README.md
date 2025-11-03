
---

# Bitcoin Block and Merkle Tree Assignment

## Task 1: Block Inspection

### Block Inspection Results
```

Block Height: 700001
Block Hash: 00000000000000000002f39baabb00ffeb47dbdb425d5077baa62c47482b7e92
Previous Block Hash: 0000000000000000000590fc0f3eba193a278534220b2b37e9849e1a770ca959
Merkle Root: 09200b2dfa12fd0626294ef8d8102d23ef09b8e52d6c24e4523a836fd153c9e7
Number of Transactions: 496
Timestamp: 1631333702

````

**Explorer Used:** [blockstream.info](https://blockstream.info/block/00000000000000000002f39baabb00ffeb47dbdb425d5077baa62c47482b7e92)

### Explanation

This block (height **700001**) is part of the Bitcoin main chain.  
It includes 496 transactions, and its **Merkle root** represents the combined cryptographic hash of all those transactions using Bitcoin’s Merkle tree structure.  
The `previousblockhash` links this block to its predecessor, ensuring chain immutability.

---

## Task 2: Merkle Tree Visualization

### Example Transactions (First 4 TXIDs)

| Label | Transaction ID |
|:------|:----------------|
| **TxA** | 754743ac0cb4a831267525aac3cc5c4b42806051c2b49b4f6a4ffd33ec3a51c5 |
| **TxB** | b128fd9a10698f56afe67d90a92ff373af90ea4e6677384ffcf4ad3f95b6ae73 |
| **TxC** | c23facdcf0a55256bc428ccb27b7ce75d1e9dcb8695332e50fbd7aa95a025263 |
| **TxD** | e551f7e65efe2a3c400e25ecf745d671cc2055c96887eca3da4c94f87c652f54 |

---

### ASCII Merkle Tree Diagram

```text
 
                                                        Merkle Root
                     b4519fe55a60df2fc81b0a1379b1758c8dfe528d14c6b920807988f621063eb0
                                        |
                     +------------------+------------------+
                     |                                     |
Hash(AB) = SHA256(SHA256(TxA + TxB))             Hash(CD) = SHA256(SHA256(TxC + TxD))
  b4519fe55a60df2fc81b0a1379b1758c8dfe528d14c6b920807988f621063eb0     85c0b8f0b9e5f407c7d1a6f7b0d2f491eab1f8d84ef44c0d0adf00c7f63adf3c
                     |                                     |
            +--------+--------+                   +--------+--------+
            |                 |                   |                 |
        TxA                TxB                 TxC                TxD
754743ac0cb4a831267525aac3cc5c4b42806051c2b49b4f6a4ffd33ec3a51c5  b128fd9a10698f56afe67d90a92ff373af90ea4e6677384ffcf4ad3f95b6ae73
c23facdcf0a55256bc428ccb27b7ce75d1e9dcb8695332e50fbd7aa95a025263  e551f7e65efe2a3c400e25ecf745d671cc2055c96887eca3da4c94f87c652f54

````

---

### Merkle Root Calculation Process

1. **Pair and concatenate adjacent transaction hashes:**

   ```
   Pair 1: TxA + TxB
   Pair 2: TxC + TxD
   ```

2. **Apply double SHA-256 to each pair:**

   ```
   Hash(AB) = SHA256(SHA256(TxA + TxB))
   Hash(CD) = SHA256(SHA256(TxC + TxD))
   ```

3. **Concatenate Hash(AB) and Hash(CD):**

   ```
   Combined = Hash(AB) + Hash(CD)
   ```

4. **Compute the final Merkle root:**

   ```
   Merkle Root = SHA256(SHA256(Combined))
   ```

5. **Result:**

   ```
   Merkle Root = b4519fe55a60df2fc81b0a1379b1758c8dfe528d14c6b920807988f621063eb0
   ```

> **Note:**
> This Merkle root was derived from the first 4 transactions only.
> The real block’s root covers **all 496 transactions**, so it will differ from the true block Merkle root.

---

## Code Used

```java
public static String buildMerkleRoot(List<String> txHashes) {
    List<String> currentLevel = new ArrayList<>(txHashes);

    while (currentLevel.size() > 1) {
        List<String> nextLevel = new ArrayList<>();
        for (int i = 0; i < currentLevel.size(); i += 2) {
            String left = currentLevel.get(i);
            String right = (i + 1 < currentLevel.size()) ? currentLevel.get(i + 1) : left;
            String combined = left + right;
            String hash = sha256Hash(sha256Hash(combined));
            nextLevel.add(hash);
        }
        currentLevel = nextLevel;
    }
    return currentLevel.getFirst();
}
```

Class File is in `code/` folder

The full code base: 

  - Repository: https://github.com/NellyNakhero/bitcoin.git
  - File: https://github.com/NellyNakhero/bitcoin/blob/main/bitcoin-learning/src/main/java/com/bitcoin/learning/bitcoin_learning/bitcoinrpc/service/BlockMerkleInspector.java

---

## Summary

Through this assignment, we:

* Extracted real Bitcoin block data using the Blockstream API
* Understood how blocks are chained via `previousblockhash`
* Visualized how a **Merkle tree** combines transaction hashes
* Learned how **double SHA-256** is used for cryptographic integrity

---

**Author:** Nelly
**Date:** November 2025
**Course:** Bitcoin Learning — Assignment 6 <Dada Dev>

```
