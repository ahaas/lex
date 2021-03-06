package org.oscii.concordance;

/**
 * Aligned sentence containing an aligned span.
 */
public class SentenceExample {

  final public AlignedSentence sentence;
  final public int sourceStart, sourceLength, targetStart, targetLength;

  public SentenceExample(AlignedSentence sentence, int sourceStart, int sourceLength, int targetStart, int targetLength) {
    this.sentence = sentence;
    this.sourceStart = sourceStart;
    this.sourceLength = sourceLength;
    this.targetStart = targetStart;
    this.targetLength = targetLength;
  }

  public static SentenceExample create(AlignedSentence s, int sourceStart, int sourceLength) {
    int targetMin = s.aligned.tokens.length;
    int targetMax = -1;
    for (int i = sourceStart; i < sourceStart + sourceLength; i++) {
      int[] a = s.getAlignment()[i];
      for (int j : a) {
        if (j < targetMin) targetMin = j;
        if (j > targetMax) targetMax = j;
      }
    }
    if (targetMax == -1) {
      targetMin = 0;
    }
    return new SentenceExample(s, sourceStart, sourceLength, targetMin, targetMax - targetMin + 1);
  }
}
