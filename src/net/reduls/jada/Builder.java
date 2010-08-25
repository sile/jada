package net.reduls.jada;

import java.util.List;
import java.util.ArrayList;

public final class Builder {
    private CodeStream[] keys;
    private NodeAllocator alloca;
    private int[] base;
    private int[] chck;
    
    private int[] tind;
    private int tindSize = 0;
    private int tindTail = 0;

    private StringBuilder tail = new StringBuilder();

    private int charcode[] = new int[0x10001];
    private CharFreq charFreqs[] = new CharFreq[0x10001];

    private int codeLimit = -1;

    public Builder(final List<String> keys) {
	this.keys = new CodeStream[keys.size()];
	for(int i=0; i < keys.size(); i++) 
	    this.keys[i] = new CodeStream(keys.get(i));

	final int nodeLimit = (int)((double)countNode()*1.5)+0x10000;
	base = new int[nodeLimit];
	chck = new int[nodeLimit];
	tind = new int[tindSize+1];
	alloca = new NodeAllocator(base, chck, codeLimit);
    }

    public Searcher build() {
	buildImpl(0, keys.length, 0);
	tind[tindTail] = tail.length();
	
	int nodeSize=0;
	for(int i=0; i < base.length; i++)
	    if(base[i] > nodeSize)
		nodeSize = base[i];
	nodeSize += codeLimit; 

	{
	    int tmpBase[] = new int[nodeSize];
	    int tmpChck[] = new int[nodeSize];
	    System.arraycopy(base, 0, tmpBase, 0, nodeSize);
	    System.arraycopy(chck, 0, tmpChck, 0, nodeSize);
	    base = tmpBase;
	    chck = tmpChck;
	}

	return new Searcher(base, chck, tind, tail.toString(), charcode);
    }

    private void buildImpl(int beg, final int end, final int rootNode) {
	if(end-beg == 1) {
	    tind[tindTail++] = tail.length();
	    base[rootNode] = -tindTail;
	    tail.append(keys[beg].rest());
	    return;
	}

	List<Integer> children = new ArrayList<Integer>();
	List<Integer> ranges   = new ArrayList<Integer>();
	do {
	    final int ch = keys[beg].peek();
	    children.add(charcode[ch]);
	    ranges.add(beg);
	    beg = endOfSameNode(beg, end);
	} while (beg != end);
	ranges.add(end);

	final int baseNode = alloca.allocate(children);
	for(int i=0; i < children.size(); i++) 
	    buildImpl(ranges.get(i), ranges.get(i+1), setNode(rootNode, baseNode, children.get(i)));
    }

    private int setNode(int node, int baseNode, int code) {
	int next   = baseNode + code;
	base[node] = baseNode;
	chck[next] = node;
	return next;
    }

    private int countNode()  {
	for(int i=0; i < 0x10001; i++)
	    charFreqs[i] = new CharFreq(i);

	final int count = countNodeImpl(0, keys.length);
	for(CodeStream cs : keys)
	    cs.reset();

        java.util.Arrays.sort(charFreqs);
	for(int i=0; i < 0x10001; i++)
	    if(charFreqs[i].count==0) {
		if(codeLimit == -1)
		    codeLimit = i+1;
		charcode[charFreqs[i].code] = codeLimit-1;
	    } else {
		charcode[charFreqs[i].code] = i;
	    }
        /*
        for(int i=0; i < 0x10001; i++)
            charcode[i]=i;
        codeLimit=0x10001;
        */
	return count;
    }
    
    private int countNodeImpl(int beg, final int end)  {
	if(end-beg == 1) {
	    tindSize++;
	    return keys[beg].read()==0 ? 0 : 1; 
	}
	
	List<Integer> ranges = new ArrayList<Integer>();
	do {
	    charFreqs[keys[beg].peek()].count++;
	    ranges.add(beg);
	    beg = endOfSameNode(beg, end);
	} while (beg != end);
	ranges.add(end);
	
	int count = ranges.size()-1;
	for(int i=0; i < ranges.size()-1; i++)
	    count += countNodeImpl(ranges.get(i), ranges.get(i+1));
	return count;
    }

    private int endOfSameNode(final int beg, final int end) {
	final int ch = keys[beg].read();
	int cur = beg+1;
	int lastCh = -1;

	for(; cur < end && ch == (lastCh=keys[cur].peek()); cur++)
	    keys[cur].read();
	return cur;
    }

    private static class CharFreq implements Comparable<CharFreq> {
	public int code;
	public int count = 0;
	
	public CharFreq(int code) { this.code = code; }
	public int compareTo(CharFreq cf) { return cf.count - count; }
    }

    private static class CodeStream {
	private final String s;
	private int p = 0;

	public CodeStream(String s) { this.s = s; }
	public int read() { return p < s.length() ? s.charAt(p++)+1 : 0; }
	public int peek() { return p < s.length() ? s.charAt(p)+1 : 0; }
	public void reset() { p = 0; }
	public String rest() { return s.substring(p); }
    }
}