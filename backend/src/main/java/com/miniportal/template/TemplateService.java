package com.miniportal.template;

import com.miniportal.common.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<Template> findAll() {
        return templateRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Template getById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vorlage " + id + " nicht gefunden."));
    }
}
