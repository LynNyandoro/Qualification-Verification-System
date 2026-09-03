package com.mim736.qvs.config;

import com.mim736.qvs.domain.Institution;
import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationStatus;
import com.mim736.qvs.domain.QualificationType;
import com.mim736.qvs.domain.Role;
import com.mim736.qvs.domain.StudentStage;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.repo.InstitutionRepository;
import com.mim736.qvs.repo.QualificationRepository;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.service.CredentialHashService;
import com.mim736.qvs.service.CredentialIds;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataSeeder {

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
            populateCampus(userAccountRepository, qualificationRepository, msu, msuIssuer, studentHash,
                    new NamedStudent("student", "Amina Chikomo", StudentStage.ALUMNI, 0));
            populateCampus(userAccountRepository, qualificationRepository, nust, nustIssuer, studentHash,
                    new NamedStudent("graduating", "Tawanda Ncube", StudentStage.GRADUATING, 16));
            populateCampus(userAccountRepository, qualificationRepository, uz, uzIssuer, studentHash,
                    new NamedStudent("freshman", "Rudo Moyo", StudentStage.ENROLLED, 5));
        };
    }

    private static void populateCampus(
            UserAccountRepository users,
            QualificationRepository qualifications,
            Institution institution,
            UserAccount issuer,
            String studentHash,
            NamedStudent featured
    ) {
        int existing = (int) users.countStudentsAtInstitution(Role.STUDENT, institution.getCode());
        int index = 0;
        while (existing < 100) {
            NamedStudent named = index == featured.slot() ? featured : null;
            String username = named != null ? named.username() : generatedUsername(institution, index);
            if (!users.existsByUsername(username)) {
                String fullName = named != null ? named.fullName() : generatedName(institution, index);
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

    private static String generatedName(Institution institution, int index) {
        String first = FIRST_NAMES[index % FIRST_NAMES.length];
        String last = LAST_NAMES[(index + institution.getCode().length()) % LAST_NAMES.length];
        return first + " " + last;
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
