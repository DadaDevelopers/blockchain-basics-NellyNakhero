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
