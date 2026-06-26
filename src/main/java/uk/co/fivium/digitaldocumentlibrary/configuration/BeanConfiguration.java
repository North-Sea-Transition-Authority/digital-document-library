package uk.co.fivium.digitaldocumentlibrary.configuration;

import org.jsoup.safety.Safelist;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BeanConfiguration {

  @Bean
  @ConditionalOnMissingBean
  Safelist documentLibraryContentSanitisationSafelist() {
    return Safelist.basic()
        .addAttributes("p", "style", "class")
        .addAttributes("div", "style", "class")
        .addTags("s");
  }

}
