package com.miniportal.template;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class TemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id")
    private Template template;

    @Column(name = "field_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType fieldType;

    private String defaultValue;

    private String partnerAttribute;

    private Integer maxLength;

    @Column(nullable = false)
    private int position;

    protected TemplateField() {
    }

    public TemplateField(String key, String label, FieldType fieldType, String defaultValue,
                         String partnerAttribute, Integer maxLength, int position) {
        this.key = key;
        this.label = label;
        this.fieldType = fieldType;
        this.defaultValue = defaultValue;
        this.partnerAttribute = partnerAttribute;
        this.maxLength = maxLength;
        this.position = position;
    }

    void assignTo(Template template) {
        this.template = template;
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getPartnerAttribute() {
        return partnerAttribute;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public int getPosition() {
        return position;
    }
}
