package com.mim736.qvs.service;

import com.mim736.qvs.domain.Qualification;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CredentialHashService {

    private CredentialHashService() {
    }

    public static String canonicalPayload(Qualification qualification) {
        return String.join("|",
                nullSafe(qualification.getHolderName()).toLowerCase(),
                nullSafe(qualification.getHolderNationalId()),
                nullSafe(qualification.getTitle()).toLowerCase(),
                qualification.getType() == null ? "" : qualification.getType().name(),
                nullSafe(qualification.getIssuingInstitution()).toLowerCase(),
                qualification.getIssueDate() == null ? "" : qualification.getIssueDate().toString(),
                qualification.getExpiryDate() == null ? "" : qualification.getExpiryDate().toString(),
                qualification.getNqfLevel() == null ? "" : qualification.getNqfLevel().toString()
        );
    }

    public static String sha256(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public static String hash(Qualification qualification) {
        return sha256(canonicalPayload(qualification));
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value.trim();
    }
}
