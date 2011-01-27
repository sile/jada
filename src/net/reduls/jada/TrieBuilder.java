package net.reduls.jada;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.util.PriorityQueue;

/**
 * DoubleArray-Trieの構築を行うクラス。
 */
public final class TrieBuilder {
    private boolean hasBuilt = false;

    private String[] keys;
    private NodeAllocator alloca;
    private int[] base;
    private int[] chck;
    
    private StringBuilder tailSB = new StringBuilder();
    private String tail;

    private int charcode[] = new int[0x10001];
    private CharFreq charFreqs[] = new CharFreq[0x10001];

    private int codeLimit = -1;

    /**
     * トライの構築対象となるキーセットを受け取り、{@link TrieBuilder}インスタンスを作成する。<br />
     * 入力キーセットは、ソート済みで各要素はユニークである必要がある。<br />
     * キーセットが上記上限を満たしていない場合の{@link #build}メソッド呼び出しの結果は未定義。
     *
     * @param keys トライ構築対象となるキーセット
     */
    public TrieBuilder(final Collection<String> keys) {
	this.keys = new String[keys.size()]; 
        int i=0;
        for(String key : keys) 
	    this.keys[i++] = key;

	final int nodeLimit = (int)((double)countNode()*1.5)+0x10000;
	base = new int[nodeLimit];
	chck = new int[nodeLimit];
	alloca = new NodeAllocator(base, chck, codeLimit);
	
	tailSB.append("\0\0");
    }

    /**
     * トライを構築する。
     * {@code build(false)}に等しい。
     *
     * @return 構築済みの{@link Trie}インスタンス
     */
    public Trie build() {
	return build(false);
    }

    /**
     * トライを構築する。
     *
     * @param shrinkTail trueならTAIL配列の圧縮を行う。圧縮した場合、TAIL配列のサイズは縮小されるが、その分構築に時間が掛かり、また構築時のメモリ消費量も多くなる。
     * @return 構築済みの{@link Trie}インスタンス
     */
    public Trie build(boolean shrinkTail) {
	if(hasBuilt==false) {
            if(keys.length != 0)
                buildImpl();//0, keys.length, 0, 0);
	    
	    int nodeSize=0;
	    for(int i=0; i < base.length; i++)
		if(base[i] > nodeSize)
		    nodeSize = base[i];
	    nodeSize += codeLimit; 
	    
	    int tmpBase[] = new int[nodeSize];
	    int tmpChck[] = new int[nodeSize];
	    System.arraycopy(base, 0, tmpBase, 0, nodeSize);
	    System.arraycopy(chck, 0, tmpChck, 0, nodeSize);
	    base = tmpBase;
	    chck = tmpChck;

	    for(int i=0; i < base.length; i++) 
		if(base[i] < 0) 
		    if(chck[i] < 0 || i == NodeAllocator.headIndex())
			base[i] = 0;
	    
	    tail = tailSB.toString();
	    tailSB.setLength(0);
	    if(shrinkTail) 
		tail = new ShrinkTail(base, tail).shrink();
	    
	    hasBuilt = true;
	}

	BitVector bv = new BitVector(base.length);
	for(int i=0; i < base.length; i++) 
	    if(base[i] < 0)
		bv.set(i, true);
	bv.buildRankIndex();
	
	return new Trie(base, chck, tail, charcode, bv);
    }
    
    private final class Node implements Comparable<Node> {
        public int rootNode;
        public int beg;
        public int end;
        public int depth;
        public List<Integer> children = new ArrayList<Integer>();
        public List<Integer> ranges   = new ArrayList<Integer>();

        public Node(int beg, int end, int node, int depth) {
            this.rootNode = node;
            this.beg = beg;
            this.end = end;
            this.depth = depth;

            if(this.end-this.beg != 1) {
            do {
                final int ch = readCode(keys[this.beg], this.depth);
                children.add(charcode[ch]);
                ranges.add(this.beg);
                this.beg = endOfSameNode(this.beg, this.end, this.depth);
            } while (this.beg != this.end);
            ranges.add(this.end);
            }
        }

        public int compareTo(Node n) {
            return n.children.size() - children.size();
        }
    }

    private void buildImpl() {
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
        queue.add(new Node(0, keys.length, 0, 0));
        while(queue.isEmpty()==false) {
            Node n = queue.poll();

            if(n.end-n.beg == 1) {
                if(rest(keys[n.beg], n.depth).isEmpty()==false) {
                    base[n.rootNode] = -tailSB.length();		
                    tailSB.append(rest(keys[n.beg], n.depth)+'\0');
                } else {
                    base[n.rootNode] = -(tailSB.length()-1);
                }
                continue;
            }
            
            final int baseNode = alloca.allocate(n.children);
            for(int i=0; i < n.children.size(); i++) 
                queue.add(new Node(n.ranges.get(i), n.ranges.get(i+1),
                                   setNode(n.rootNode, baseNode, n.children.get(i)), 
                                   n.depth+1));
        }
    }    

    private void buildImpl(int beg, final int end, final int rootNode, final int depth) {
	if(end-beg == 1) {
	    if(rest(keys[beg], depth).isEmpty()==false) {
		base[rootNode] = -tailSB.length();		
		tailSB.append(rest(keys[beg], depth)+'\0');
	    } else {
		base[rootNode] = -(tailSB.length()-1);
	    }
	    return;
	}

	List<Integer> children = new ArrayList<Integer>();
	List<Integer> ranges   = new ArrayList<Integer>();
	do {
	    final int ch = readCode(keys[beg], depth);
	    children.add(charcode[ch]);
	    ranges.add(beg);
	    beg = endOfSameNode(beg, end, depth);
	} while (beg != end);
	ranges.add(end);

	final int baseNode = alloca.allocate(children);
	for(int i=0; i < children.size(); i++) 
	    buildImpl(ranges.get(i), ranges.get(i+1), 
                      setNode(rootNode, baseNode, children.get(i)), depth+1);
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

	final int count = keys.length==0 ? 0 : countNodeImpl(0, keys.length, 0);

        java.util.Arrays.sort(charFreqs);
	for(int i=0; i < 0x10001; i++)
	    if(charFreqs[i].count==0) {
		if(codeLimit == -1)
		    codeLimit = i+1;
		charcode[charFreqs[i].code] = 0;
	    } else {
		charcode[charFreqs[i].code] = i+1;
	    }
	return count;
    }
    
    private int countNodeImpl(int beg, final int end, final int depth)  {
	if(end-beg == 1) 
	    return readCode(keys[beg], depth)==0 ? 0 : 1; 
	
	List<Integer> ranges = new ArrayList<Integer>();
	do {
	    charFreqs[readCode(keys[beg], depth)].count++;
	    ranges.add(beg);
	    beg = endOfSameNode(beg, end, depth);
	} while (beg != end);
	ranges.add(end);
	
	int count = ranges.size()-1;
	for(int i=0; i < ranges.size()-1; i++)
	    count += countNodeImpl(ranges.get(i), ranges.get(i+1), depth+1);
	return count;
    }

    private int endOfSameNode(final int beg, final int end, final int depth) {
	final int ch = readCode(keys[beg], depth);
	int cur = beg+1;

	for(; cur < end && ch == readCode(keys[cur], depth); cur++);
	return cur;
    }

    public int readCode(String s, int depth) {
        return depth < s.length() ? s.charAt(depth)+1 : 0; 
    }
    
    public String rest(String s, int depth) {
        return depth < s.length() ? s.substring(depth) : "";
    }

    /**
     * 入力キーセット内の各文字のコード値と出現頻度を保持するクラス。
     */
    private static class CharFreq implements Comparable<CharFreq> {
	public int code;
	public int count = 0;
	
	public CharFreq(int code) { this.code = code; }
	public int compareTo(CharFreq cf) { return cf.count - count; }
    }
}