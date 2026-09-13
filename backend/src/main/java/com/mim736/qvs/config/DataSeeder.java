package com.mim736.qvs.config;

import com.mim736.qvs.domain.Institution;
import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationStatus;
import com.mim736.qvs.domain.QualificationType;
import com.mim736.qvs.domain.Role;
import com.mim736.qvs.domain.StudentStage;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.domain.VerificationRecord;
import com.mim736.qvs.domain.VerificationResult;
import com.mim736.qvs.repo.InstitutionRepository;
import com.mim736.qvs.repo.QualificationRepository;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.repo.VerificationRecordRepository;
import com.mim736.qvs.service.CredentialHashService;
import com.mim736.qvs.service.CredentialIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class DataSeeder {

    private static final Logger LOG = LoggerFactory.getLogger(DataSeeder.class);
    private static final int TARGET_AUDIT_EVENTS = 80;
    private static final Pattern TRAILING_INDEX = Pattern.compile("(\\d+)$");
    private static final String[] MIDDLE_NAMES = {
            "Ade", "Itai", "Lee", "Kai", "Neo", "Joy", "Ann", "Ivy", "Max", "Ari"
    };

    private static final String[] FIRST_NAMES = {
            "Amina", "Tendai", "Nyasha", "Farai", "Chido", "Kudzai", "Tatenda", "Panashe", "Blessing", "Tanaka",
            "Vimbai", "Simba", "Rumbidzai", "Tadiwa", "Rudo", "Tinashe", "Chiedza", "Kudakwashe", "Rufaro", "Sharon",
            "Tawanda", "Anesu", "Mufaro", "Rutendo", "Tariro", "Nokutenda", "Takudzwa", "Chenai", "Shamiso", "Tadiwanashe"
    };

    private static final String[] LAST_NAMES = {
            "Chikomo", "Dube", "Mutasa", "Sibanda", "Makoni", "Mlilo", "Gwara", "Nyoni", "Mpofu", "Chirwa",
            "Ndlovu", "Mukaro", "Zhou", "Moyo", "Ncube", "Gumbo", "Mhlanga", "Sithole", "Banda", "Nkomo",
            "Pena", "Chari", "Mapfumo", "Hove", "Zimunya", "Kanyenze", "Mufuka", "Chigumira", "Mashiri", "Nyandoro"
    };

    private static final List<Programme> PROGRAMMES = List.of(
            new Programme("MISM", "Master of Information Systems Management", QualificationType.DEGREE, 9),
            new Programme("BSCS", "Bachelor of Science in Computer Science", QualificationType.DEGREE, 8),
            new Programme("LLB", "Bachelor of Laws", QualificationType.DEGREE, 8),
            new Programme("BCOM", "Bachelor of Commerce in Accounting", QualificationType.DEGREE, 8),
            new Programme("BENG", "BEng Industrial and Manufacturing Engineering", QualificationType.DEGREE, 8),
            new Programme("BEDU", "Bachelor of Education", QualificationType.DEGREE, 8)
    );

    @Bean
    CommandLineRunner seedUsers(
            InstitutionRepository institutionRepository,
            UserAccountRepository userAccountRepository,
            QualificationRepository qualificationRepository,
            VerificationRecordRepository verificationRecordRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Institution uz = upsertInstitution(institutionRepository, "UZ", "University of Zimbabwe");
            Institution nust = upsertInstitution(institutionRepository, "NUST",
                    "National University of Science and Technology");
            Institution msu = upsertInstitution(institutionRepository, "MSU", "Midlands State University");

            upsertUser(userAccountRepository, passwordEncoder,
                    "admin", "admin@qvs.local", "System Administrator", Role.ADMIN, "Admin@123", null, null);
            UserAccount msuIssuer = upsertUser(userAccountRepository, passwordEncoder,
                    "issuer", "issuer@msu.ac.zw", "MSU Registrar", Role.ISSUER, "Issuer@123", msu, null);
            UserAccount nustIssuer = upsertUser(userAccountRepository, passwordEncoder,
                    "nust", "registrar@nust.ac.zw", "NUST Registrar", Role.ISSUER, "Nust@123", nust, null);
            UserAccount uzIssuer = upsertUser(userAccountRepository, passwordEncoder,
                    "uz", "registrar@uz.ac.zw", "UZ Registrar", Role.ISSUER, "Uz@123", uz, null);

            upsertUser(userAccountRepository, passwordEncoder,
                    "econet", "talent@econet.co.zw", "Econet Wireless Zimbabwe", Role.VERIFIER, "Econet@123",
                    null, null);
            upsertUser(userAccountRepository, passwordEncoder,
                    "cbz", "recruitment@cbz.co.zw", "CBZ Holdings", Role.VERIFIER, "Cbz@123", null, null);
            upsertUser(userAccountRepository, passwordEncoder,
                    "delta", "careers@delta.co.zw", "Delta Corporation", Role.VERIFIER, "Delta@123", null, null);
            upsertUser(userAccountRepository, passwordEncoder,
                    "verifier", "hr@demo-employer.co.zw", "Demo Employer HR", Role.VERIFIER, "Verifier@123",
                    null, null);

            upsertUser(userAccountRepository, passwordEncoder, "student", "amina.chikomo@msu.ac.zw",
                    "Amina Chikomo", Role.STUDENT, "Student@123", msu, StudentStage.ALUMNI);
            upsertUser(userAccountRepository, passwordEncoder, "graduating", "tawanda.ncube@nust.ac.zw",
                    "Tawanda Ncube", Role.STUDENT, "Graduating@123", nust, StudentStage.GRADUATING);
            upsertUser(userAccountRepository, passwordEncoder, "freshman", "rudo.moyo@uz.ac.zw",
                    "Rudo Moyo", Role.STUDENT, "Freshman@123", uz, StudentStage.ENROLLED);

            String studentHash = passwordEncoder.encode("Student@123");
            Set<String> takenNames = new HashSet<>();
            takenNames.add("amina chikomo");
            takenNames.add("tawanda ncube");
            takenNames.add("rudo moyo");
            populateCampus(userAccountRepository, qualificationRepository, msu, msuIssuer, studentHash,
                    new NamedStudent("student", "Amina Chikomo", StudentStage.ALUMNI, 0), takenNames);
            populateCampus(userAccountRepository, qualificationRepository, nust, nustIssuer, studentHash,
                    new NamedStudent("graduating", "Tawanda Ncube", StudentStage.GRADUATING, 16), takenNames);
            populateCampus(userAccountRepository, qualificationRepository, uz, uzIssuer, studentHash,
                    new NamedStudent("freshman", "Rudo Moyo", StudentStage.ENROLLED, 5), takenNames);

            uniquifyStudentNames(userAccountRepository, qualificationRepository);

            applyDemoStatuses(qualificationRepository);
            seedVerificationHistory(verificationRecordRepository, qualificationRepository, userAccountRepository);
            LOG.info(
                    "Demo catalog ready: {} students, {} credentials, {} verification events",
                    userAccountRepository.findByRoleOrderByFullNameAsc(Role.STUDENT).size(),
                    qualificationRepository.count(),
                    verificationRecordRepository.count()
            );
        };
    }

    private static void populateCampus(
            UserAccountRepository users,
            QualificationRepository qualifications,
            Institution institution,
            UserAccount issuer,
            String studentHash,
            NamedStudent featured,
            Set<String> takenNames
    ) {
        int existing = (int) users.countStudentsAtInstitution(Role.STUDENT, institution.getCode());
        int index = 0;
        while (existing < 100) {
            NamedStudent named = index == featured.slot() ? featured : null;
            String username = named != null ? named.username() : generatedUsername(institution, index);
            if (!users.existsByUsername(username)) {
                String fullName = named != null
                        ? named.fullName()
                        : allocateUniqueName(username, institution, takenNames);
                StudentStage stage = named != null ? named.stage() : stageFor(index);
                String email = emailFor(username, institution);
                if (!users.existsByEmail(email)) {
                    createStudent(users, username, email, fullName, studentHash, institution, stage);
                    existing++;
                }
            }
            index++;
            if (index > 500) {
                break;
            }
        }

        UserAccount featuredAccount = users.findByUsername(featured.username()).orElse(null);
        if (featuredAccount != null && featuredAccount.getInstitution() == null) {
            featuredAccount.setInstitution(institution);
            featuredAccount.setStudentStage(featured.stage());
            featuredAccount = users.save(featuredAccount);
        }

        if (featuredAccount != null && featured.stage() == StudentStage.ALUMNI) {
            awardIfMissing(qualifications, users, issuer, featuredAccount, PROGRAMMES.get(0), LocalDate.of(2024, 11, 15));
            awardIfMissing(qualifications, users, issuer, featuredAccount,
                    new Programme("CISA", "Certified Information Systems Auditor", QualificationType.PROFESSIONAL, 8),
                    LocalDate.of(2025, 6, 1));
        }

        List<UserAccount> students = users.findStudentsAtInstitution(Role.STUDENT, institution.getCode());
        for (int i = 0; i < students.size(); i++) {
            UserAccount student = students.get(i);
            if (featured.username().equals(student.getUsername())) {
                continue;
            }
            if (student.getStudentStage() == StudentStage.ENROLLED) {
                continue;
            }
            if (student.getStudentStage() == StudentStage.GRADUATING && i % 3 != 0) {
                continue;
            }
            Programme programme = PROGRAMMES.get(i % PROGRAMMES.size());
            awardIfMissing(qualifications, users, issuer, student, programme, LocalDate.of(2024, 6, 15).minusDays(i));
        }
    }

    private static void uniquifyStudentNames(
            UserAccountRepository users,
            QualificationRepository qualifications
    ) {
        List<UserAccount> students = new ArrayList<>(users.findByRoleOrderByFullNameAsc(Role.STUDENT));
        students.sort((left, right) -> left.getUsername().compareToIgnoreCase(right.getUsername()));
        Set<String> taken = new HashSet<>();
        for (String featured : List.of("student", "graduating", "freshman")) {
            users.findByUsername(featured).ifPresent(account -> {
                if (account.getFullName() != null && !account.getFullName().isBlank()) {
                    taken.add(account.getFullName().toLowerCase(Locale.ROOT));
                }
            });
        }
        int renamed = 0;
        for (UserAccount student : students) {
            if ("student".equals(student.getUsername())
                    || "graduating".equals(student.getUsername())
                    || "freshman".equals(student.getUsername())) {
                continue;
            }
            String current = student.getFullName() == null ? "" : student.getFullName().trim();
            String key = current.toLowerCase(Locale.ROOT);
            if (!current.isBlank() && taken.add(key)) {
                continue;
            }
            String unique = allocateUniqueName(student.getUsername(), student.getInstitution(), taken);
            student.setFullName(unique);
            users.save(student);
            for (Qualification qualification : qualifications.findByHolderUsernameIgnoreCaseOrderByCreatedAtDesc(
                    student.getUsername())) {
                qualification.setHolderName(unique);
                qualification.setCredentialHash(CredentialHashService.hash(qualification));
                qualification.setUpdatedAt(Instant.now());
                qualifications.save(qualification);
            }
            renamed++;
        }
        if (renamed > 0) {
            LOG.info("Assigned unique full names to {} seeded students", renamed);
        }
    }

    private static String allocateUniqueName(String username, Institution institution, Set<String> taken) {
        int seed = extractIndex(username);
        if (institution != null) {
            seed += Math.abs(institution.getCode().hashCode()) * 17;
        }
        for (int attempt = 0; attempt < 10_000; attempt++) {
            String candidate = composeName(seed + attempt);
            if (taken.add(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
        String fallback = "Student " + username;
        taken.add(fallback.toLowerCase(Locale.ROOT));
        return fallback;
    }

    private static String composeName(int id) {
        int safe = id == Integer.MIN_VALUE ? 0 : Math.abs(id);
        int grid = FIRST_NAMES.length * LAST_NAMES.length;
        String first = FIRST_NAMES[safe % FIRST_NAMES.length];
        String last = LAST_NAMES[(safe / FIRST_NAMES.length) % LAST_NAMES.length];
        int cycle = safe / grid;
        if (cycle <= 0) {
            return first + " " + last;
        }
        String middle = MIDDLE_NAMES[(cycle - 1) % MIDDLE_NAMES.length];
        return first + " " + middle + " " + last;
    }

    private static int extractIndex(String username) {
        Matcher matcher = TRAILING_INDEX.matcher(username == null ? "" : username);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return Math.abs((username == null ? "student" : username).hashCode());
    }

    private static StudentStage stageFor(int index) {
        if (index < 20) {
            return StudentStage.ENROLLED;
        }
        if (index < 30) {
            return StudentStage.GRADUATING;
        }
        return StudentStage.ALUMNI;
    }

    private static String generatedUsername(Institution institution, int index) {
        return institution.getCode().toLowerCase() + String.format("%03d", index);
    }

    private static void createStudent(
            UserAccountRepository repository,
            String username,
            String email,
            String fullName,
            String passwordHash,
            Institution institution,
            StudentStage stage
    ) {
        if (repository.existsByUsername(username) || repository.existsByEmail(email)) {
            return;
        }
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setEmail(email);
        account.setFullName(fullName);
        account.setRole(Role.STUDENT);
        account.setEnabled(true);
        account.setPasswordHash(passwordHash);
        account.setInstitution(institution);
        account.setStudentStage(stage);
        repository.save(account);
    }

    private static void awardIfMissing(
            QualificationRepository repository,
            UserAccountRepository users,
            UserAccount issuer,
            UserAccount student,
            Programme programme,
            LocalDate issueDate
    ) {
        if (repository.existsByHolderUsernameIgnoreCaseAndTitle(student.getUsername(), programme.title())) {
            return;
        }
        String shareId = CredentialIds.next(repository, programme.code(), student.getInstitution().getCode());
        String verificationCode = "student".equals(student.getUsername()) && "MISM".equals(programme.code())
                ? CredentialIds.DEMO_VERIFICATION_CODE
                : CredentialIds.nextVerificationCode(repository);
        Qualification qualification = new Qualification();
        qualification.setCredentialId(shareId);
        qualification.setVerificationCode(verificationCode);
        qualification.setHolderName(student.getFullName());
        qualification.setHolderUsername(student.getUsername());
        qualification.setHolderNationalId(nationalIdFor(student.getUsername()));
        qualification.setTitle(programme.title());
        qualification.setType(programme.type());
        qualification.setIssuingInstitution(student.getInstitution().getName());
        qualification.setIssueDate(issueDate);
        qualification.setNqfLevel(programme.nqfLevel());
        qualification.setStatus(QualificationStatus.ACTIVE);
        qualification.setRegisteredBy(issuer);
        qualification.setCredentialHash(CredentialHashService.hash(qualification));
        repository.save(qualification);
        if (student.getStudentStage() == StudentStage.GRADUATING) {
            student.setStudentStage(StudentStage.ALUMNI);
            users.save(student);
        }
    }

    private static String nationalIdFor(String username) {
        int stamp = Math.abs(username.hashCode() % 900000) + 100000;
        return "63-" + stamp + "-A-12";
    }

    private static String emailFor(String username, Institution institution) {
        String domain = institution.getCode().toLowerCase() + ".ac.zw";
        return username.replace('.', '-') + "@" + domain;
    }

    private static void applyDemoStatuses(QualificationRepository repository) {
        List<Qualification> all = repository.findAll();
        boolean hasRevoked = all.stream().anyMatch(item -> item.getStatus() == QualificationStatus.REVOKED);
        boolean hasExpired = all.stream().anyMatch(item -> item.getStatus() == QualificationStatus.EXPIRED);
        if (hasRevoked && hasExpired) {
            return;
        }
        int index = 0;
        for (Qualification qualification : all) {
            if ("student".equals(qualification.getHolderUsername())) {
                continue;
            }
            index++;
            if (!hasRevoked && index % 17 == 0) {
                qualification.setStatus(QualificationStatus.REVOKED);
                qualification.setUpdatedAt(Instant.now());
                repository.save(qualification);
            } else if (!hasExpired && index % 19 == 0) {
                qualification.setExpiryDate(LocalDate.of(2023, 12, 31));
                qualification.setStatus(QualificationStatus.EXPIRED);
                qualification.setCredentialHash(CredentialHashService.hash(qualification));
                qualification.setUpdatedAt(Instant.now());
                repository.save(qualification);
            }
        }
    }

    private static void seedVerificationHistory(
            VerificationRecordRepository records,
            QualificationRepository qualifications,
            UserAccountRepository users
    ) {
        long existing = records.count();
        if (existing >= TARGET_AUDIT_EVENTS) {
            return;
        }
        List<UserAccount> verifiers = List.of("econet", "cbz", "delta", "verifier").stream()
                .map(username -> users.findByUsername(username).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        List<Qualification> catalog = qualifications.findAll();
        if (verifiers.isEmpty() || catalog.isEmpty()) {
            return;
        }
        List<Qualification> active = catalog.stream()
                .filter(item -> item.getStatus() == QualificationStatus.ACTIVE)
                .toList();
        List<Qualification> revoked = catalog.stream()
                .filter(item -> item.getStatus() == QualificationStatus.REVOKED)
                .toList();
        List<Qualification> expired = catalog.stream()
                .filter(item -> item.getStatus() == QualificationStatus.EXPIRED)
                .toList();
        List<Qualification> validPool = active.isEmpty() ? catalog : active;

        Instant now = Instant.now();
        int needed = TARGET_AUDIT_EVENTS - (int) existing;
        List<VerificationRecord> batch = new ArrayList<>(needed);
        for (int i = 0; i < needed; i++) {
            UserAccount verifier = verifiers.get(i % verifiers.size());
            int kind = i % 10;
            VerificationRecord record = new VerificationRecord();
            record.setVerifiedBy(verifier);
            record.setVerifiedAt(now.minus(Duration.ofHours(4L * i + 2)));
            if (kind == 0) {
                record.setResult(VerificationResult.NOT_FOUND);
                record.setMethod("CODE");
                record.setVerificationCodeAttempted("QVS-FAKE" + String.format("%05d", i));
                record.setNotes("No matching record");
            } else if (kind == 1 && !revoked.isEmpty()) {
                Qualification qualification = revoked.get(i % revoked.size());
                fillAttempt(record, qualification, "CREDENTIAL_ID");
                record.setResult(VerificationResult.REVOKED);
                record.setNotes("This qualification has been revoked by the issuing authority.");
            } else if (kind == 2 && !expired.isEmpty()) {
                Qualification qualification = expired.get(i % expired.size());
                fillAttempt(record, qualification, "CODE");
                record.setResult(VerificationResult.EXPIRED);
                record.setNotes("This qualification has expired.");
            } else if (kind == 3) {
                Qualification qualification = validPool.get(i % validPool.size());
                fillAttempt(record, qualification, "HASH");
                record.setResult(VerificationResult.TAMPERED);
                record.setNotes("Stored integrity hash does not match the canonical credential payload.");
            } else if (kind == 4) {
                Qualification qualification = validPool.get(i % validPool.size());
                fillAttempt(record, qualification, "CANDIDATE_NAME");
                record.setResult(VerificationResult.INVALID);
                record.setNotes("Candidate details did not match the stored credential.");
            } else {
                Qualification qualification = validPool.get(i % validPool.size());
                String method = switch (i % 3) {
                    case 0 -> "CODE";
                    case 1 -> "CREDENTIAL_ID";
                    default -> "CANDIDATE_NAME";
                };
                fillAttempt(record, qualification, method);
                record.setResult(VerificationResult.VALID);
                record.setNotes("Qualification is authentic and currently active.");
            }
            batch.add(record);
        }
        records.saveAll(batch);
    }

    private static void fillAttempt(VerificationRecord record, Qualification qualification, String method) {
        record.setQualification(qualification);
        record.setCredentialIdAttempted(qualification.getCredentialId());
        record.setVerificationCodeAttempted(qualification.getVerificationCode());
        record.setMethod(method);
    }

    private static Institution upsertInstitution(InstitutionRepository repository, String code, String name) {
        return repository.findByCode(code).orElseGet(() -> {
            Institution institution = new Institution();
            institution.setCode(code);
            institution.setName(name);
            return repository.save(institution);
        });
    }

    private static UserAccount upsertUser(
            UserAccountRepository repository,
            PasswordEncoder encoder,
            String username,
            String email,
            String fullName,
            Role role,
            String rawPassword,
            Institution institution,
            StudentStage studentStage
    ) {
        return repository.findByUsername(username).map(existing -> {
            boolean dirty = false;
            if (existing.getInstitution() == null && institution != null) {
                existing.setInstitution(institution);
                dirty = true;
            }
            if (existing.getStudentStage() == null && studentStage != null) {
                existing.setStudentStage(studentStage);
                dirty = true;
            }
            return dirty ? repository.save(existing) : existing;
        }).orElseGet(() -> {
            UserAccount account = new UserAccount();
            account.setUsername(username);
            account.setEmail(email);
            account.setFullName(fullName);
            account.setRole(role);
            account.setEnabled(true);
            account.setPasswordHash(encoder.encode(rawPassword));
            account.setInstitution(institution);
            account.setStudentStage(studentStage);
            return repository.save(account);
        });
    }

    private record Programme(String code, String title, QualificationType type, int nqfLevel) {
    }

    private record NamedStudent(String username, String fullName, StudentStage stage, int slot) {
    }
}
