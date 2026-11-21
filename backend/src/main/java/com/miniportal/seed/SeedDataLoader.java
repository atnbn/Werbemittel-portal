package com.miniportal.seed;

import com.miniportal.partner.Partner;
import com.miniportal.partner.PartnerRepository;
import com.miniportal.security.Role;
import com.miniportal.security.User;
import com.miniportal.security.UserRepository;
import com.miniportal.template.FieldType;
import com.miniportal.template.Template;
import com.miniportal.template.TemplateField;
import com.miniportal.template.TemplateRepository;
import com.miniportal.template.TemplateType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedDataLoader implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "demo";
    private static final String CLAIM = "Gemeinsam mehr erreichen.";

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataLoader(PartnerRepository partnerRepository, UserRepository userRepository,
                          TemplateRepository templateRepository, PasswordEncoder passwordEncoder) {
        this.partnerRepository = partnerRepository;
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        seedTemplates();
    }

    private void seedUsers() {
        Partner partner1 = partnerRepository.save(
                new Partner("Autohaus Müller", "Hauptstr. 1", "96106", "Ebern", "09531 12345", 10_000));
        Partner partner2 = partnerRepository.save(
                new Partner("Fahrrad Schmidt", "Bahnhofstr. 5", "96047", "Bamberg", "0951 98765", 500));

        String passwordHash = passwordEncoder.encode(DEMO_PASSWORD);
        userRepository.save(new User("zentrale", passwordHash, Role.ZENTRALE, null));
        userRepository.save(new User("partner1", passwordHash, Role.PARTNER, partner1.getId()));
        userRepository.save(new User("partner2", passwordHash, Role.PARTNER, partner2.getId()));
    }

    private void seedTemplates() {
        Template flyer = new Template("Flyer Frühling", TemplateType.FLYER, 12, 50);
        flyer.addField(fixedClaim());
        flyer.addField(partnerData("companyName", "Firmenname", "companyName", 2));
        flyer.addField(editable("headline", "Überschrift", "Jetzt vorbeikommen!", 40, 3));
        templateRepository.save(flyer);

        Template poster = new Template("Plakat Sommer", TemplateType.POSTER, 250, 40);
        poster.addField(fixedClaim());
        poster.addField(partnerData("companyName", "Firmenname", "companyName", 2));
        poster.addField(partnerData("phone", "Telefon", "phone", 3));
        poster.addField(editable("headline", "Überschrift", "Sommerangebote", 30, 4));
        templateRepository.save(poster);

        Template newspaperAd = new Template("Zeitungsanzeige Herbst", TemplateType.NEWSPAPER_AD, 4_900, 30);
        newspaperAd.addField(fixedClaim());
        newspaperAd.addField(partnerData("companyName", "Firmenname", "companyName", 2));
        newspaperAd.addField(partnerData("city", "Ort", "city", 3));
        newspaperAd.addField(editable("offer", "Angebotstext", "Jetzt Termin vereinbaren.", 60, 4));
        templateRepository.save(newspaperAd);
    }

    private static TemplateField fixedClaim() {
        return new TemplateField("claim", "Marken-Claim", FieldType.FIXED, CLAIM, null, null, 1);
    }

    private static TemplateField partnerData(String key, String label, String partnerAttribute, int position) {
        return new TemplateField(key, label, FieldType.PARTNER_DATA, null, partnerAttribute, null, position);
    }

    private static TemplateField editable(String key, String label, String defaultValue, int maxLength, int position) {
        return new TemplateField(key, label, FieldType.EDITABLE, defaultValue, null, maxLength, position);
    }
}
