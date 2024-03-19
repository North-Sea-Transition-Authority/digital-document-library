package uk.co.fivium.digitaldocumentlibrary.document;

import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
class ContentSanitisationService {

  private static final String BASE_URI = "";

  private final Cleaner basicCleaner;

  ContentSanitisationService() {
    this.basicCleaner = new Cleaner(Safelist.basic());
  }

  String getSanitisedContent(String content) {
    var dirty = Jsoup.parseBodyFragment(content, BASE_URI);
    return basicCleaner.clean(dirty).body().html();
  }

}
