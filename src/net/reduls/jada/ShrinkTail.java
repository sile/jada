package net.reduls.jada;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

final class ShrinkTail {
    private final String tail;
    private int base[];
    private List<TTT> ss = new ArrayList<TTT>();

    public ShrinkTail(int base[], final String tail, final int keyCount) {
	this.tail = tail;
	this.base = base;
	
	for(int i=0; i < base.length; i++) 
	    if(base[i] < 0) {
		final int beg = -base[i];
		final int end = tail.indexOf('\0',beg);
		ss.add(new TTT(tail.substring(beg,end), i));
	    }
    }

    public String shrink() {
	Collections.sort(ss);

	StringBuilder newTail = new StringBuilder();
	newTail.append("\0\0");
	for(int i=0; i < ss.size(); i++) {
	    final TTT t = ss.get(i);
	    int pos = newTail.length();
	    if(i>0 && ss.get(i-1).including(t))
		pos -= t.s.length()+1; // +1 is necessary for last '\0' character
	    else
		newTail.append(t.s+'\0');
	    base[t.i] = -pos;
	}
	return newTail.toString();
    }

    private static class TTT implements Comparable<TTT> {
	public String s;
	public int i;
	
	public TTT(String s, int i) {
	    this.s = s;
	    this.i = i;
	}

	public boolean including(TTT t) {
	    int i=s.length()-1;
	    int j=t.s.length()-1;

	    for(;; i--, j--) {
		if(j < 0) return true;
		if(i < 0) return false;
		if(s.charAt(i) != t.s.charAt(j)) return false;
	    }	    
	}

	public int compareTo(TTT t) {
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
