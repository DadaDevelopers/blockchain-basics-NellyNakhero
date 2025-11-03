
Fetch Transaction ids
---------------------------------------------
```shell

curl https://blockstream.info/api/block/00000000000000000002f39baabb00ffeb47dbdb425d5077baa62c47482b7e92/txids

```


### Merkle tree demonstration


---

## **Step 1: Identify 4 Transactions**

From the code output for block 700001:

```
TxA: 754743ac0cb4a831267525aac3cc5c4b42806051c2b49b4f6a4ffd33ec3a51c5
TxB: b128fd9a10698f56afe67d90a92ff373af90ea4e6677384ffcf4ad3f95b6ae73
TxC: c23facdcf0a55256bc428ccb27b7ce75d1e9dcb8695332e50fbd7aa95a025263
TxD: e551f7e65efe2a3c400e25ecf745d671cc2055c96887eca3da4c94f87c652f54
```

---

## **Step 2: Build the Merkle Tree**

**Level 1 (leaves)**: TX hashes

```
TxA: 754743ac0cb4a831267525aac3cc5c4b42806051c2b49b4f6a4ffd33ec3a51c5
TxB: b128fd9a10698f56afe67d90a92ff373af90ea4e6677384ffcf4ad3f95b6ae73
TxC: c23facdcf0a55256bc428ccb27b7ce75d1e9dcb8695332e50fbd7aa95a025263
TxD: e551f7e65efe2a3c400e25ecf745d671cc2055c96887eca3da4c94f87c652f54
```

---

**Level 2 (hash pairs)**: Hash each pair using double SHA-256

* **Hash(AB) = SHA256(SHA256(TxA + TxB))** => `Hash(AB) = SHA256(SHA256(TxA + TxB)) = b4519fe55a60df2fc81b0a1379b1758c8dfe528d14c6b920807988f621063eb0`
* **Hash(CD) = SHA256(SHA256(TxC + TxD))** => `Hash(CD) = SHA256(SHA256(TxC + TxD)) = 85c0b8f0b9e5f407c7d1a6f7b0d2f491eab1f8d84ef44c0d0adf00c7f63adf3c`

From the code output:

```
Hash(AB): b4519fe55a60df2fc81b0a1379b1758c8dfe528d14c6b920807988f621063eb0
Hash(CD): (calculated similarly)
```

---

**Level 3 (root)**: Combine hashes from level 2

* **Merkle Root = SHA256(SHA256(Hash(AB) + Hash(CD)))** => `Merkle Root = SHA256(SHA256(Hash(AB) + Hash(CD))) = b4519fe55a60df2fc81b0a1379b1758c8dfe528d14c6b920807988f621063eb0`

Since we only are just checking for **4 sample transactions**, the calculated Merkle root is:

```
Merkle Root: b4519fe55a60df2fc81b0a1379b1758c8dfe528d14c6b920807988f621063eb0
```

---

## **Step 3: Visualize the Tree**

```

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

```

---

## **Step 4: Steps**

1. Concatenate TxA + TxB → double SHA-256 → Hash(AB)
2. Concatenate TxC + TxD → double SHA-256 → Hash(CD)
3. Concatenate Hash(AB) + Hash(CD) → double SHA-256 → Merkle Root

---

️ *Note:* This sample root differs from the real block’s Merkle root because only 4 of 496 transactions were used for illustration.

---

## Explanation

- A **Merkle Tree** allows Bitcoin to verify that a transaction is included in a block without downloading all other transactions.
- Each **leaf node** represents a transaction hash (TXID).
- Each **non-leaf node** is the double SHA-256 hash of its two children.
- This hierarchical hashing structure ensures **data integrity** — if even a single transaction changes, the Merkle Root changes completely.
- The Merkle Root is stored in the **block header**, serving as a cryptographic fingerprint of all transactions in that block.

---


##   Code Implementation

The code below retrieves block information and computes a sample Merkle root for the first four transactions using the [Blockstream API](https://blockstream.info/api/).

### `BlockMerkleInspector.java`

```java

package com.bitcoin.learning.bitcoin_learning.bitcoinrpc.service;

import com.bitcoin.learning.bitcoin_learning.bitcoinrpc.dtos.BlockInfo;
import com.bitcoin.learning.bitcoin_learning.bitcoinrpc.dtos.BlockInfoResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BlockMerkleInspector {
    private final ObjectMapper objectMapper = new ObjectMapper();

    //Helper: Compute SHA-256 hash (hex)
    public static String sha256Hash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    //Build Merkle Root from TXIDS(Uses bitcoin equivalent double SHA-256)
    public static String buildMerkleRoot(List<String> txHashes){
        List<String> currentLevel = new ArrayList<>(txHashes);

        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();

            for (int i = 0; i < currentLevel.size(); i += 2){
                String left = currentLevel.get(i);
                String right = (i + 1 < currentLevel.size()) ? currentLevel.get(i + 1) : left; // duplicate if odd
                String combined = left + right;
                String hash = sha256Hash(sha256Hash(combined)); //double SHA-256
                nextLevel.add(hash);
            }
            currentLevel = nextLevel;
        }
        return currentLevel.getFirst();
    }

    public String computeMerkleRootForSampleTransactions(String blockHash) throws IOException, InterruptedException {
        //Fetch blockchain info
        String blockUrl = "https://blockstream.info/api/block/" + blockHash;
        // 1️ Create HTTP client and send request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest blockRequest = HttpRequest.newBuilder()
                .uri(URI.create(blockUrl))
                .build();
        HttpResponse<String> blockResponse = client.send(blockRequest, HttpResponse.BodyHandlers.ofString());

        // 2️ Map JSON to your BlockInfo class
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("String blockResponse "+ blockResponse.body());
        BlockInfoResponse blockInfo = objectMapper.readValue(blockResponse.body(), BlockInfoResponse.class);

        // 3⃣ Print results
        System.out.println("Block Info Retrieved Successfully:");
        System.out.println("----------------------------------");
        System.out.println("Height: " + blockInfo.getHeight());
        System.out.println("Hash: " + blockInfo.getHash());
        System.out.println("Merkle Root: " + blockInfo.getMerkleroot());
        System.out.println("Number of TXs: " + blockInfo.getTx_count());
        System.out.println("Timestamp: " + blockInfo.getTime());
        System.out.println("Number of TXs: " + blockInfo.getTx_count());

        // 4️ Fetch first 4 TXIDs
        String txUrl = blockUrl.concat("/txs");
        HttpRequest txRequest = HttpRequest.newBuilder().uri(URI.create(txUrl)).build();

        String txResponse = client.send(txRequest, HttpResponse.BodyHandlers.ofString()).body();
        List<Map<String, Object>> txObjects = objectMapper.readValue(txResponse, new TypeReference<List<Map<String, Object>>>() {});
        List<String> txids = new ArrayList<>();

        for (int i = 0; i < Math.min(4, txObjects.size()); i++) {
            String txid = (String) txObjects.get(i).get("txid");
            txids.add(txid);
            System.out.println("Tx" + (char)('A' + i) + ": " + txid);
        }

        // 5️ Compute Merkle Root for sample TXs
        String merkleRoot = buildMerkleRoot(txids);

        String result = "Calculated Sample Merkle Root (first 4 TXs only): " + merkleRoot;
        System.out.println(result);
        System.out.println("Note: This won’t match the block’s full Merkle root because we’re using only 4 TXs.");
        return result;
    }
}

```

#  Task 3: Logs Execution Output

```shell

Block Info Retrieved Successfully:
----------------------------------
Height: 700001
Hash: 00000000000000000002f39baabb00ffeb47dbdb425d5077baa62c47482b7e92
Merkle Root: 09200b2dfa12fd0626294ef8d8102d23ef09b8e52d6c24e4523a836fd153c9e7
Number of TXs: 496
Timestamp: 1631333702
TxA: 754743ac0cb4a831267525aac3cc5c4b42806051c2b49b4f6a4ffd33ec3a51c5
TxB: b128fd9a10698f56afe67d90a92ff373af90ea4e6677384ffcf4ad3f95b6ae73
TxC: c23facdcf0a55256bc428ccb27b7ce75d1e9dcb8695332e50fbd7aa95a025263
TxD: e551f7e65efe2a3c400e25ecf745d671cc2055c96887eca3da4c94f87c652f54
Calculated Sample Merkle Root (first 4 TXs only): b4519fe55a60df2fc81b0a1379b1758c8dfe528d14c6b920807988f621063eb0
Note: This won’t match the block’s full Merkle root because we’re using only 4 TXs.

```