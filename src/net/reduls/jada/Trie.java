package net.reduls.jada;

import java.io.IOException;

public final class Trie {
    private final int base[];
    private final int chck[];
    private final String tail;
    private final int charcode[];
    private final BitVector bv;

    Trie(int base[], int chck[], String tail, int charcode[], BitVector bv) {
	this.base = base;
	this.chck = chck;
	this.tail = tail;
	this.charcode = charcode;
	this.bv = bv;
    }

    public int nodeCount() { return base.length; }
    public int tailLength() { return tail.length(); }
    public int keyCount() { return bv.rank(base.length); }

    public int search(final String key) {
	int node = 0;
	for(int i=0;; i++) {
	    if(base[node] < 0)
		return tailEqual(-base[node], key, i) ? bv.rank(node) : -1;
	    
	    final int next = base[node] + code(key, i);
	    if(chck[next] == node) node = next;
	    else                   return -1; 
	}
    }

    public void save(final String filepath) throws IOException {
	FileMappedOutputStream out = 
	    new FileMappedOutputStream(filepath, 
				       bv.dataSize() +
				       2 * 4 + 
				       (base.length+chck.length+charcode.length)*4 +
				       tail.length()*2);
	try {
	    bv.write(out);
	    
	    out.putInt(base.length);
	    out.putInt(tail.length());
	    
	    for(int i=0; i < 0x10000;     i++) out.putInt(charcode[i]);
	    for(int i=0; i < base.length; i++) out.putInt(base[i]);
	    for(int i=0; i < chck.length; i++) out.putInt(chck[i]);
	    out.putString(tail);
	} finally {
	    out.close();
	}
    }

    public static Trie load(final String filepath) throws IOException {
	FileMappedInputStream in = 
	    new FileMappedInputStream(filepath);
	try {
	    final BitVector bv = new BitVector(in);
	    
	    final int nodeSize = in.getInt();
	    final int tailSize = in.getInt();
	    
	    final int charcode[] = in.getIntArray(0x10000);
	    final int base[] = in.getIntArray(nodeSize);
	    final int chck[] = in.getIntArray(nodeSize);
	    final String tail = in.getString(tailSize);
	    
	    return new Trie(base, chck, tail, charcode, bv);
	} finally {
	    in.close();
	}
    }
    
    private int code(final String key, final int i) {
	return charcode[(key.length()==i ? 0 : key.charAt(i)+1)];
    }

    private boolean tailEqual(final int tailHead, final String key, final int i) {
	if(i >= key.length())
	    return true;

	final int limit = key.length()-i;
	for(int j=0; j < limit; j++)
	    if(key.charAt(i+j) != tail.charAt(tailHead+j))
		return false;
	return tail.charAt(tailHead+limit)=='\0';
    }
}