package com.miniportal.order;

import com.miniportal.partner.Partner;
import com.miniportal.template.Template;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id")
    private Template template;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private long totalCents;

    @Column(nullable = false)
    private long subsidyCents;

    @Column(nullable = false)
    private long ownShareCents;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<OrderFieldValue> fieldValues = new ArrayList<>();

    protected Order() {
    }

    public Order(Partner partner, Template template, int quantity, PriceBreakdown price,
                 Map<String, String> values, Instant createdAt) {
        this.partner = partner;
        this.template = template;
        this.quantity = quantity;
        this.status = OrderStatus.BESTELLT;
        this.totalCents = price.totalCents();
        this.subsidyCents = price.subsidyCents();
        this.ownShareCents = price.ownShareCents();
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        values.forEach((key, value) -> {
            OrderFieldValue fieldValue = new OrderFieldValue(key, value);
            fieldValue.assignTo(this);
            fieldValues.add(fieldValue);
        });
    }

    public Long getId() {
        return id;
    }

    public Partner getPartner() {
        return partner;
    }

    public Template getTemplate() {
        return template;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public long getSubsidyCents() {
        return subsidyCents;
    }

    public long getOwnShareCents() {
        return ownShareCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderFieldValue> getFieldValues() {
        return List.copyOf(fieldValues);
    }
}
