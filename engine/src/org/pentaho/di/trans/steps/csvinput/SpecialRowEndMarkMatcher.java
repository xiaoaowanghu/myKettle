package org.pentaho.di.trans.steps.csvinput;

public class SpecialRowEndMarkMatcher implements PatternMatcherInterface {

  public CrLfMatcherInterface crlfMatcher;
  private MultiBytePatternMatcher patternMatcher;
  private boolean isLastTimePatternMatched;
  
  public SpecialRowEndMarkMatcher(CrLfMatcherInterface _crlfMatcher) {
	  patternMatcher = new MultiBytePatternMatcher(); 
	  this.crlfMatcher = _crlfMatcher;
	  isLastTimePatternMatched = false;
  }
	
  @Override
  public boolean matchesPattern(byte[] source, int sourceSize, int location, byte[] pattern) {
	if( isLastTimePatternMatched && patternMatcher.getContinueToMatchIndex() == 0 ) {
	  if( crlfMatcher.isLineFeed(source, location) || crlfMatcher.isReturn(source, location) ) {
		reset();
	    return true;
      } 
	}
	
	isLastTimePatternMatched = patternMatcher.matchesPattern(source, sourceSize, location, pattern);
	return false;
  }
  
  public void reset() {
	isLastTimePatternMatched = false;
	patternMatcher.reset();
  }

}
