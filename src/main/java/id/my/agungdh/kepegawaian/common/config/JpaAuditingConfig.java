package id.my.agungdh.kepegawaian.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    AuditorAware<Long> auditorProvider() {
        // Sementara kosong karena modul user/auth belum ada.
        // Ganti dengan pembacaan dari SecurityContext setelah modul user tersedia.
        return Optional::empty;
    }
}
