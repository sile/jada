package net.reduls.jada;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * DoubleArray-TrieのTAIL配列の圧縮を行うクラス。
 */
final class ShrinkTail {
    private final String tail;
    private int base[];
    private List<StringIndexPair> pairs = new ArrayList<StringIndexPair>();

    /**
     * TAIL圧縮に必要な情報を受け取り、{@link ShrinkTail}インスタンスを初期化する。
     *
     * @param base トライのBASE配列。{@link #shrink}メソッド呼び出しにより破壊的に修正される。
     * @param tail 圧縮元となるTAIL配列
     */
    public ShrinkTail(int base[], final String tail) {
	this.tail = tail;
	this.base = base;
	
	for(int i=0; i < base.length; i++) 
	    if(base[i] < 0) {
		final int beg = -base[i];
		final int end = tail.indexOf('\0',beg);
		pairs.add(new StringIndexPair(tail.substring(beg,end), i));
	    }
    }

    /**
     * TAIL配列の圧縮を行う。<br />
     * 本メソッドの呼び出しに伴い、{@link #base}配列に保持されている各キーの末尾文字列へのインデックス値は破壊的に修正される。
     *
     * @return 圧縮後のTAIL配列
     */
    public String shrink() {
	Collections.sort(pairs);

	StringBuilder newTail = new StringBuilder();
	newTail.append("\0\0");
	for(int i=0; i < pairs.size(); i++) {
	    final StringIndexPair t = pairs.get(i);
	    int pos = newTail.length();
	    if(i>0 && pairs.get(i-1).including(t))
		pos -= t.s.length()+1; // +1 is necepairsary for last '\0' character
	    else
		newTail.append(t.s+'\0');
	    base[t.i] = -pos;
	}
	return newTail.toString();
    }

    /**
     * キーの接尾文字列とそのTAIL配列内での開始インデックスとの対応を保持するクラス。
     */
    private static class StringIndexPair implements Comparable<StringIndexPair> {
	public final String s;
	public final int i;
	
	public StringIndexPair(String s, int i) {
	    this.s = s;
	    this.i = i;
	}

	public boolean including(StringIndexPair t) {
	    int i=s.length()-1;
	    int j=t.s.length()-1;

	    for(;; i--, j--) {
		if(j < 0) return true;
		if(i < 0) return false;
		if(s.charAt(i) != t.s.charAt(j)) return false;
	    }	    
	}

	public int compareTo(StringIndexPair t) {
	    int i=s.length()-1;
	    int j=t.s.length()-1;

	    for(;; i--, j--) {
		if(i < 0 && j < 0) return  0;
		if(i < 0)          return  1;
		if(j < 0)          return -1;
		if(s.charAt(i) > t.s.charAt(j)) return -1;
		if(s.charAt(i) < t.s.charAt(j)) return  1;
	    }
	}
    }
}
