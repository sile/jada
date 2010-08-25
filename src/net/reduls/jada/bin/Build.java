package net.reduls.jada.bin;

import net.reduls.jada.Builder;
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

	List<String> keys = new ArrayList<String>();
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	for(String line=br.readLine(); line!=null; line=br.readLine())
	    keys.add(line);

	new Builder(keys).build().save(args[0]);
    }
}