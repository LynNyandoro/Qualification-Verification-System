package com.mim736.qvs.config;

import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationStatus;
import com.mim736.qvs.domain.QualificationType;
import com.mim736.qvs.domain.Role;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.repo.QualificationRepository;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.service.CredentialHashService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(
            UserAccountRepository userAccountRepository,
            QualificationRepository qualificationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            UserAccount admin = upsertUser(userAccountRepository, passwordEncoder,
                    "admin", "admin@qvs.local", "System Administrator", Role.ADMIN, "Admin@123");
            UserAccount issuer = upsertUser(userAccountRepository, passwordEncoder,
                    "issuer", "issuer@qvs.local", "Institution Registrar", Role.ISSUER, "Issuer@123");
            upsertUser(userAccountRepository, passwordEncoder,
                    "verifier", "verifier@qvs.local", "Employer Verifier", Role.VERIFIER, "Verifier@123");

            if (qualificationRepository.count() == 0) {
                Qualification sample = new Qualification();
                sample.setCredentialId("11111111-1111-1111-1111-111111111111");
                sample.setVerificationCode("QVS-DEMO12345");
                sample.setHolderName("Amina Chikomo");
                sample.setHolderNationalId("63-123456-A-12");
                sample.setTitle("Master of Information Systems Management");
                sample.setType(QualificationType.DEGREE);
                sample.setIssuingInstitution("Midlands State University");
                sample.setIssueDate(LocalDate.of(2024, 11, 15));
                sample.setNqfLevel(9);
                sample.setStatus(QualificationStatus.ACTIVE);
                sample.setRegisteredBy(issuer.getId() == null ? admin : issuer);
                sample.setCredentialHash(CredentialHashService.hash(sample));
                qualificationRepository.save(sample);
            }
        };
    }

    private static UserAccount upsertUser(
            UserAccountRepository repository,
            PasswordEncoder encoder,
            String username,
            String email,
            String fullName,
            Role role,
            String rawPassword
    ) {
        return repository.findByUsername(username).orElseGet(() -> {
            UserAccount account = new UserAccount();
            account.setUsername(username);
            account.setEmail(email);
            account.setFullName(fullName);
            account.setRole(role);
            account.setEnabled(true);
            account.setPasswordHash(encoder.encode(rawPassword));
            return repository.save(account);
        });
    }
}
