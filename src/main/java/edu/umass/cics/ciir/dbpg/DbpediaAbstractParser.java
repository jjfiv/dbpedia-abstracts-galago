package edu.umass.cics.ciir.dbpg;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.utility.Parameters;

import java.io.BufferedReader;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Note that this doesn't parse the RDF Turtle Format correctly, but since we're just shoving it into a search engine quotes and escaping shouldn't be a big deal.
 * @author jfoley.
 */
public class DbpediaAbstractParser extends DocumentStreamParser {
  private BufferedReader reader;

  public DbpediaAbstractParser(DocumentSplit split, Parameters p) throws IOException {
    super(split, p);
    this.reader = DocumentStreamParser.getBufferedReader(split);
  }

  final static String RelationType = " <http://dbpedia.org/ontology/abstract> ";

  @Override
  public Document nextDocument() throws IOException {
    if (reader == null)
      return null;
    while (true) {
      String data = reader.readLine();
      //System.err.println("# "+data);
      if (data == null)
        return null;
      if (!data.contains(RelationType))
        continue;

      String[] kv = data.split(RelationType);
      String title = removeSurrounding(compactSpaces(kv[0]), "<http://dbpedia.org/resource/", ">");
      String text = removeSurrounding(compactSpaces(kv[1]), "\"", "\"@en .");

      // don't generate documents for empty abstracts
      if (text.isEmpty())
        continue;

      Document doc = new Document();
      doc.name = title;
      doc.text = "<title>" + title.replaceAll("_", " ") + "</title>\n<body>" + text + "</body>";

      return doc;
    }
  }

  @Override
  public void close() throws IOException {
    reader.close();
    reader = null;
  }

  /** Remove prefix and suffix from input string */
  @Nonnull
  public static String removeSurrounding(String input, String prefix, String suffix) {
    if (!input.endsWith(suffix))
      return removeFront(input, prefix);
    if (!input.startsWith(prefix))
      return removeBack(input, suffix);
    return input.substring(prefix.length(), input.length() - suffix.length());
  }

  /** Remove ending from input string */
  @Nonnull
  public static String removeBack(String input, String suffix) {
    if (!input.endsWith(suffix))
      return input;
    return input.substring(0, input.length() - suffix.length());
  }

  /** Remove prefix from input string */
  public static String removeFront(String input, String prefix) {
    if (!input.startsWith(prefix))
      return input;
    return input.substring(prefix.length());
  }

  /** Simplify input string in terms of spaces; all space characters -&gt; ' ' and a maximum width of 1 space. */
  @Nonnull
  public static String compactSpaces(CharSequence input) {
    StringBuilder sb = new StringBuilder();
    boolean lastWasSpace = true;
    for (int i = 0; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (Character.isWhitespace(ch)) {
        if (lastWasSpace)
          continue;
        sb.append(' ');
        lastWasSpace = true;
        continue;
      }
      lastWasSpace = false;
      sb.append(ch);
    }
    if (lastWasSpace) {
      return sb.toString().trim();
    }
    return sb.toString();
  }
}
