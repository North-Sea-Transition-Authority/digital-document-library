package uk.co.fivium.digitaldocumentlibrary.document;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
class ContentSanitisationService {

  private static final String BASE_URI = "";

  private final Cleaner basicCleaner;

  ContentSanitisationService(Safelist documentLibraryContentSanitisationSafeList) {
    this.basicCleaner = new Cleaner(documentLibraryContentSanitisationSafeList);
  }

  String getSanitisedContent(String content) {
    var dirty = Jsoup.parseBodyFragment(content, BASE_URI);
    // If the content contains any self-closing or non-closed html elements e.g. <br/>, the output will yield a
    // non-closed br <br> by default, causing openhtmltopdf to break when parsing the content of a document.
    // Setting this to XML means the output will contain elements which are closed.
    dirty.outputSettings()
        .syntax(Document.OutputSettings.Syntax.xml)
        .prettyPrint(false); // we don't care about the pretty format of the output

    return basicCleaner.clean(dirty).body().html();
  }

}
