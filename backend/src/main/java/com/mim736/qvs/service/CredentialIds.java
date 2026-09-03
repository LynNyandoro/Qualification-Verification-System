package com.mim736.qvs.service;

import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.repo.QualificationRepository;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

public final class CredentialIds {

    public static final String DEMO_VERIFICATION_CODE = "QVS-DEMO12345";
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern NON_LETTERS = Pattern.compile("[^A-Z]+");

    private CredentialIds() {
    }

    public static String programCode(String title) {
        if (title == null || title.isBlank()) {
            return "CRED";
        }
        String upper = title.toUpperCase(Locale.ROOT);
        if (upper.contains("INFORMATION SYSTEMS MANAGEMENT") || upper.contains("INFORMATION SECURITY MANAGEMENT")) {
            return "MISM";
        }
        if (upper.contains("COMPUTER SCIENCE")) {
            return "BSCS";
        }
        if (upper.contains("LAWS") || upper.contains("LLB")) {
            return "LLB";
        }
        if (upper.contains("ACCOUNT")) {
            return "BCOM";
        }
        if (upper.contains("INDUSTRIAL") || upper.contains("MANUFACTURING") || upper.contains("PRODUCTION")) {
            return "BENG";
        }
        if (upper.contains("ELECTRONIC")) {
            return "BTEE";
        }
        if (upper.contains("EDUCATION")) {
            return "BEDU";
        }
        if (upper.contains("AUDITOR") || upper.contains("CISA")) {
            return "CISA";
        }
        if (upper.contains("PROFESSIONAL CERTIFICATE") || upper.contains("CERTIFICATE")) {
            return "CERT";
        }
        if (upper.contains("DIPLOMA")) {
            return "DIPL";
        }
        String letters = NON_LETTERS.matcher(upper).replaceAll("");
        if (letters.length() >= 4) {
            return letters.substring(0, 4);
        }
        return letters.isBlank() ? "CRED" : letters;
    }

    public static String institutionCode(UserAccount registrar, String institutionName) {
        if (registrar != null && registrar.getInstitution() != null) {
            return registrar.getInstitution().getCode();
        }
        if (institutionName == null) {
            return "QVS";
        }
        String upper = institutionName.toUpperCase(Locale.ROOT);
        if (upper.contains("MIDLANDS")) {
            return "MSU";
        }
        if (upper.contains("SCIENCE AND TECHNOLOGY") || upper.contains("NUST")) {
            return "NUST";
        }
        if (upper.contains("UNIVERSITY OF ZIMBABWE") || upper.equals("UZ")) {
            return "UZ";
        }
        return "QVS";
    }

    public static String next(QualificationRepository repository, String programCode, String institutionCode) {
        String prefix = programCode + "-" + institutionCode + "-";
        int sequence = (int) repository.countByCredentialIdStartingWith(prefix) + 1;
        String candidate = format(prefix, sequence);
        while (repository.existsByCredentialId(candidate) || repository.existsByVerificationCode(candidate)) {
            sequence++;
            candidate = format(prefix, sequence);
        }
        return candidate;
    }

    public static String nextVerificationCode(QualificationRepository repository) {
        for (int attempt = 0; attempt < 40; attempt++) {
            StringBuilder builder = new StringBuilder("QVS-");
            for (int i = 0; i < 10; i++) {
                builder.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            String code = builder.toString();
            if (!repository.existsByVerificationCode(code) && !repository.existsByCredentialId(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique verification code");
    }

    private static String format(String prefix, int sequence) {
        return prefix + String.format(Locale.ROOT, "%04d", sequence);
    }
}
