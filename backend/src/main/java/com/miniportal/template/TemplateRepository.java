package com.miniportal.template;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    @EntityGraph(attributePaths = "fields")
    List<Template> findAllByOrderByIdAsc();

    @Override
    @EntityGraph(attributePaths = "fields")
    Optional<Template> findById(Long id);
}
