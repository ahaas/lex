package org.oscii.lex;

/**
 * A definition of an expression
 */
public class Definition {
    String text;
    String languageTag;

    public Definition(String text, String languageTag) {
        this.text = text;
        this.languageTag = languageTag;
    }

    @Override
    public String toString() {
        return "Definition{" +
                "text='" + text + '\'' +
                ", languageTag='" + languageTag + '\'' +
                '}';
    }
}
