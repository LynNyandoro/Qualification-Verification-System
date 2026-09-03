package com.mim736.qvs.web;

import com.mim736.qvs.repo.InstitutionRepository;
import com.mim736.qvs.web.dto.InstitutionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    private final InstitutionRepository institutionRepository;

    public InstitutionController(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @GetMapping
    public List<InstitutionResponse> list() {
        return institutionRepository.findAll().stream().map(InstitutionResponse::from).toList();
    }
}
