package com.mim736.qvs.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mim736.qvs.domain.QualificationType;
import com.mim736.qvs.web.dto.LoginRequest;
import com.mim736.qvs.web.dto.QualificationRequest;
import com.mim736.qvs.web.dto.RegisterRequest;
import com.mim736.qvs.web.dto.VerifyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QualificationVerificationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void issuerCanRegisterSearchAndVerifierCanConfirmAuthenticity() throws Exception {
        String issuerToken = login("issuer", "Issuer@123");
        String verifierToken = login("verifier", "Verifier@123");

        QualificationRequest request = new QualificationRequest();
        request.setHolderName("Tawanda Ncube");
        request.setHolderNationalId("08-998877-B-21");
        request.setTitle("Bachelor of Science in Computer Science");
        request.setType(QualificationType.DEGREE);
        request.setIssuingInstitution("National University of Science and Technology");
        request.setIssueDate(LocalDate.of(2023, 12, 10));
        request.setNqfLevel(8);

        MvcResult created = mockMvc.perform(post("/api/qualifications")
                        .header("Authorization", "Bearer " + issuerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holderName", is("Tawanda Ncube")))
                .andReturn();

        String verificationCode = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("verificationCode").asText();

        mockMvc.perform(get("/api/qualifications")
                        .header("Authorization", "Bearer " + verifierToken)
                        .param("holderName", "Tawanda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Bachelor of Science in Computer Science"));

        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setVerificationCode(verificationCode);

        mockMvc.perform(post("/api/verify")
                        .header("Authorization", "Bearer " + verifierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("VALID"))
                .andExpect(jsonPath("$.hashMatch").value(true));

        mockMvc.perform(get("/api/audit")
                        .header("Authorization", "Bearer " + verifierToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].result").value("VALID"));
    }

    @Test
    void unknownCodeIsNotFoundAndAudited() throws Exception {
        String token = login("verifier", "Verifier@123");
        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setVerificationCode("QVS-DOESNOTEX");

        mockMvc.perform(post("/api/verify")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("NOT_FOUND"));
    }

    @Test
    void verifierCannotRegisterQualification() throws Exception {
        String token = login("verifier", "Verifier@123");
        QualificationRequest request = new QualificationRequest();
        request.setHolderName("Unauthorized");
        request.setTitle("Fake Diploma");
        request.setType(QualificationType.DIPLOMA);
        request.setIssuingInstitution("Unknown");
        request.setIssueDate(LocalDate.of(2022, 1, 1));

        mockMvc.perform(post("/api/qualifications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerAndLoginWorkForNewVerifier() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newverifier");
        registerRequest.setEmail("newverifier@qvs.local");
        registerRequest.setPassword("SecurePass1");
        registerRequest.setFullName("New Verifier");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("VERIFIER"));

        login("newverifier", "SecurePass1");
    }

    @Test
    void issuerCanRevokeAndVerificationReportsRevoked() throws Exception {
        String issuerToken = login("issuer", "Issuer@123");
        String verifierToken = login("verifier", "Verifier@123");

        QualificationRequest request = new QualificationRequest();
        request.setHolderName("Revoked Holder");
        request.setTitle("Professional Certificate");
        request.setType(QualificationType.CERTIFICATE);
        request.setIssuingInstitution("ZIMCHE");
        request.setIssueDate(LocalDate.of(2021, 3, 1));

        MvcResult created = mockMvc.perform(post("/api/qualifications")
                        .header("Authorization", "Bearer " + issuerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        String code = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("verificationCode").asText();

        mockMvc.perform(put("/api/qualifications/" + id + "/revoke")
                        .header("Authorization", "Bearer " + issuerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));

        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setVerificationCode(code);
        mockMvc.perform(post("/api/verify")
                        .header("Authorization", "Bearer " + verifierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("REVOKED"));
    }

    private String login(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
