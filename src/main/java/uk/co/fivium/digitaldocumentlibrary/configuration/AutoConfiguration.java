package uk.co.fivium.digitaldocumentlibrary.configuration;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan("uk.co.fivium.digitaldocumentlibrary")
@EnableScheduling
class AutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  Clock clock() {
    return Clock.systemDefaultZone();
  }
}
