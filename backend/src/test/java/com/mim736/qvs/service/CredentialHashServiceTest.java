package com.mim736.qvs.service;

import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialHashServiceTest {

    @Test
    void hashIsDeterministicForSamePayload() {
        Qualification first = sample();
        Qualification second = sample();
        assertEquals(CredentialHashService.hash(first), CredentialHashService.hash(second));
    }

    @Test
    void hashChangesWhenTitleChanges() {
        Qualification original = sample();
        Qualification changed = sample();
        changed.setTitle("Bachelor of Commerce");
        assertNotEquals(CredentialHashService.hash(original), CredentialHashService.hash(changed));
    }

    @Test
    void sha256Produces64HexCharacters() {
        String digest = CredentialHashService.sha256("payload");
        assertEquals(64, digest.length());
        assertTrue(digest.matches("[0-9a-f]+"));
    }

    private static Qualification sample() {
        Qualification qualification = new Qualification();
        qualification.setHolderName("Amina Chikomo");
        qualification.setHolderNationalId("63-123456-A-12");
        qualification.setTitle("Master of Information Systems Management");
        qualification.setType(QualificationType.DEGREE);
        qualification.setIssuingInstitution("Midlands State University");
        qualification.setIssueDate(LocalDate.of(2024, 11, 15));
        qualification.setNqfLevel(9);
        return qualification;
    }
}
