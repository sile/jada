package net.reduls.jada;

import java.io.IOException;

public final class Searcher {
    private final int base[];
    private final int chck[];
    private final int tind[];
    private final String tail;
    private final int charcode[];

    Searcher(int base[], int chck[], int tind[], String tail, int charcode[]) {
	this.base = base;
	this.chck = chck;
	this.tind = tind;
	this.tail = tail;
	this.charcode = charcode;
    }

    public int search(final String key) {
	int node = 0;
	for(int i=0;; i++) {
	    if(base[node] < 0) {
		final int id = (-base[node])-1;
		return tailEqual(id, key, i) ? id : -1;
	    }
	    
	    final int next = base[node] + code(key, i);
	    if(chck[next] == node) node = next;
	    else                   return -1; 
	}
    }

    public void save(final String filepath) throws IOException {
	FileMappedOutputStream out = 
	    new FileMappedOutputStream(filepath, 
				       3 * 4 + 
				       (base.length+chck.length+tind.length+charcode.length)*4 +
				       tail.length()*2);
	try {
	    out.putInt(base.length);
	    out.putInt(tind.length);
	    out.putInt(tail.length());
	    
	    for(int i=0; i < 0x10000;     i++) out.putInt(charcode[i]);
	    for(int i=0; i < base.length; i++) out.putInt(base[i]);
	    for(int i=0; i < chck.length; i++) out.putInt(chck[i]);
	    for(int i=0; i < tind.length; i++) out.putInt(tind[i]);
	    out.putString(tail);
	} finally {
	    out.close();
	}
    }

    public static Searcher load(final String filepath) throws IOException {
	FileMappedInputStream in = 
	    new FileMappedInputStream(filepath);
	try {
	    final int nodeSize = in.getInt();
	    final int tindSize = in.getInt();
	    final int tailSize = in.getInt();
	    
	    final int charcode[] = in.getIntArray(0x10000);
	    final int base[] = in.getIntArray(nodeSize);
	    final int chck[] = in.getIntArray(nodeSize);
	    final int tind[] = in.getIntArray(tindSize);
	    final String tail = in.getString(tailSize);
	    
	    return new Searcher(base, chck, tind, tail, charcode);
	} finally {
	    in.close();
	}
    }
    
    private int code(final String key, final int i) {
	return charcode[(key.length()==i ? 0 : key.charAt(i)+1)];
    }

    private boolean tailEqual(final int id, final String key, final int i) {
	if(i >= key.length())
	    return true;
	final String s1 = tail.substring(tind[id],tind[id+1]);
	final String s2 = key.substring(i);
	return s1.equals(s2);
    }
}