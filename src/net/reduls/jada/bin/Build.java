package net.reduls.jada.bin;

import net.reduls.jada.TrieBuilder;
import net.reduls.jada.Trie;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public final class Build {
    public static void main(String[] args) throws IOException {
	if(args.length != 1) {
	    System.err.println("Usage: java net.reduls.jada.bin.Build index < unique-sorted-key-set");
	    System.exit(1);
	}
	Time t;

	System.err.println("= Read key set");
	t = new Time();
	List<String> keys = new ArrayList<String>();
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	for(String line=br.readLine(); line!=null; line=br.readLine())
	    keys.add(line);
	System.err.println("  == "+keys.size()+" keys");
	System.err.println("DONE ("+t.elapsed()+" ms)");
	System.err.println("");

	System.err.println("= Build index ");
	t = new Time();
	System.err.println("  == initialize");
	TrieBuilder bld = new TrieBuilder(keys);
	System.err.println("  == build");
	Trie trie = bld.build();
	System.err.println("    === node count:  "+trie.nodeCount());
	System.err.println("    === tail length: "+trie.tailLength());
	System.err.println("  == save");
	trie.save(args[0]);
	System.err.println("DONE ("+t.elapsed()+" ms)");
    }

    private static class Time {
	private final long beg_t = System.currentTimeMillis();
	public long elapsed() { return System.currentTimeMillis()-beg_t; }
    }
}