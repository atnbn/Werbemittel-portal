package com.miniportal.order;

import com.miniportal.common.NotFoundException;
import com.miniportal.partner.Partner;
import com.miniportal.partner.PartnerService;
import com.miniportal.security.AuthenticatedUser;
import com.miniportal.template.Template;
import com.miniportal.template.TemplateService;
import com.miniportal.template.TemplateValueValidator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final TemplateService templateService;
    private final PartnerService partnerService;
    private final TemplateValueValidator templateValueValidator = new TemplateValueValidator();
    private final PriceCalculator priceCalculator = new PriceCalculator();
    private final FieldValueResolver fieldValueResolver = new FieldValueResolver();

    public OrderService(OrderRepository orderRepository, TemplateService templateService,
                        PartnerService partnerService) {
        this.orderRepository = orderRepository;
        this.templateService = templateService;
        this.partnerService = partnerService;
    }

    @Transactional
    public Order placeOrder(Long partnerId, Long templateId, int quantity, Map<String, String> values) {
        Template template = templateService.getById(templateId);
        templateValueValidator.validate(template, values);
        PriceBreakdown price = priceCalculator.calculate(
                quantity, template.getUnitPriceCents(), template.getSubsidyPercent());

        Partner partner = partnerService.getForUpdate(partnerId);
        partner.deductBalance(price.subsidyCents());

        Map<String, String> snapshot = fieldValueResolver.resolve(template.getFields(), values, partner::getAttribute);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        return orderRepository.save(new Order(partner, template, quantity, price, snapshot, now));
    }

    @Transactional(readOnly = true)
    public Order getVisibleOrder(Long orderId, AuthenticatedUser viewer) {
        return orderRepository.findById(orderId)
                .filter(order -> isVisibleTo(order, viewer))
                .orElseThrow(() -> new NotFoundException("Bestellung " + orderId + " nicht gefunden."));
    }

    private static boolean isVisibleTo(Order order, AuthenticatedUser viewer) {
        return switch (viewer.getRole()) {
            case ZENTRALE -> true;
            case PARTNER -> order.getPartner().getId().equals(viewer.getPartnerId());
        };
    }
}
