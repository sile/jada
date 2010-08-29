package net.reduls.jada.bin;

import net.reduls.jada.Trie;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.io.Reader;
import java.io.StringReader;

public final class Search {
    public static void main(String[] args) throws IOException {
	if(!(args.length==1 || (args.length==2 && args[0].equals("-p")))) {
	    System.err.println("Usage: java net.reduls.jada.bin.Search [-p] index < key-list > key-id-list");
	    System.exit(1);
	}

	final Trie srch = Trie.load(args.length==1 ? args[0] : args[1]);
	if(args.length==1) {
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    for(String line=br.readLine(); line!=null; line=br.readLine())
		System.out.println(srch.search(line));
	} else {
	    Time t;
	    
	    System.err.println("= Read key list");
	    t = new Time(); 

	    List<String> keys = new ArrayList<String>();
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    for(String line=br.readLine(); line!=null; line=br.readLine())
		keys.add(line);
	    System.err.println("  == "+keys.size()+" keys");
	    System.err.println("DONE ("+t.elapsed()+" ms)");
	    System.err.println("");

            for(int k=0; k < 6; k++) {
                System.err.println("= Search"+": "+k);
                t = new Time(); 
                int fails=0;
                for(String key : keys)
                    if(srch.search(key)==-1)
                        fails++;
                System.err.println("  == failed: "+fails+"/"+keys.size());
                System.err.println("DONE ("+t.elapsed()+" ms)");
            }
	}
    }

    private static class Time {
	private final long beg_t = System.currentTimeMillis();
	public long elapsed() { return System.currentTimeMillis()-beg_t; }
    }
}