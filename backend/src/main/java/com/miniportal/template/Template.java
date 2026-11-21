package com.miniportal.template;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType type;

    @Column(nullable = false)
    private long unitPriceCents;

    @Column(nullable = false)
    private int subsidyPercent;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<TemplateField> fields = new ArrayList<>();

    protected Template() {
    }

    public Template(String name, TemplateType type, long unitPriceCents, int subsidyPercent) {
        this.name = name;
        this.type = type;
        this.unitPriceCents = unitPriceCents;
        this.subsidyPercent = subsidyPercent;
    }

    public void addField(TemplateField field) {
        field.assignTo(this);
        fields.add(field);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TemplateType getType() {
        return type;
    }

    public long getUnitPriceCents() {
        return unitPriceCents;
    }

    public int getSubsidyPercent() {
        return subsidyPercent;
    }

    public List<TemplateField> getFields() {
        return List.copyOf(fields);
    }
}
