package uk.co.fivium.digitaldocumentlibrary.configuration;

import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.util.XRLog;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan("uk.co.fivium.digitaldocumentlibrary")
@EnableScheduling
class AutoConfiguration {

  AutoConfiguration() {
    // https://github.com/danfickle/openhtmltopdf/wiki/Logging#slf4j-logging-adapter
    XRLog.setLoggerImpl(new Slf4jLogger());
  }

  @Bean
  @ConditionalOnMissingBean
  Clock clock() {
    return Clock.systemDefaultZone();
  }
}
