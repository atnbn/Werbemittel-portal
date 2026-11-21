package com.miniportal.template;

import com.miniportal.template.dto.TemplateResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public List<TemplateResponse> findAll() {
        return templateService.findAll().stream().map(TemplateResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TemplateResponse getById(@PathVariable Long id) {
        return TemplateResponse.from(templateService.getById(id));
    }
}
