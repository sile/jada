package net.reduls.jada.bin;

import net.reduls.jada.Searcher;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public final class Search {
    public static void main(String[] args) throws IOException {
	if(args.length != 1) {
	    System.err.println("Usage: java net.reduls.jada.bin.Search index < key-list > key-id-list");
	    System.exit(1);
	}

	final Searcher srch = Searcher.load(args[0]);
	
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	for(String line=br.readLine(); line!=null; line=br.readLine())
	    System.out.println(srch.search(line));
    }
}