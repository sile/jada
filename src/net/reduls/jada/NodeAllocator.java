package net.reduls.jada;

import java.util.List;

final class NodeAllocator {
    private int freeNext[];
    private int freePrev[];
    
    public NodeAllocator(int base[], int chck[], final int codeLimit) {
        freeNext = base;
        freePrev = chck;
	
	for(int i=0; i < codeLimit; i++) 
	    freePrev[i] = freeNext[i] = 1;

	for(int i=codeLimit; i < base.length; i++) {
	    freePrev[i]=-(i-1);
	    freeNext[i]=-(i+1);
	}
        
        freeNext[headIndex()] = -codeLimit;
	freePrev[codeLimit] = -headIndex();
    }
    
    public static int headIndex() { return 1; }
    
    public int allocate(final List<Integer> children) {
	int cur = -freeNext[headIndex()];
	final int first = children.get(0);
	
	for(;; cur = -freeNext[-freeNext[-freeNext[cur]]]) {
	    int x = cur - first;
	    if(canAllocate(x, children)) {
		for(Integer code : children)
		    allocate(x+code);
		return x;
	    }
	}
    }

    private boolean canAllocate(final int x, final List<Integer> children) {
	for(int i=1; i < children.size(); i++) 
	    if(freePrev[x+children.get(i)]>=0) 
		return false;
	return true;
    }

    private void allocate(final int node) {
	freePrev[-freeNext[node]] = freePrev[node];
	freeNext[-freePrev[node]] = freeNext[node];
	freePrev[node] = freeNext[node] = 1;	
    }
}
