package net.reduls.jada;

import java.io.IOException;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

final class FileMappedInputStream {
    private final FileChannel cnl;
    private int cur=0;

    public FileMappedInputStream(String filepath) throws IOException {
	cnl = new FileInputStream(filepath).getChannel();
    }

    public int getInt() throws IOException {
	return map(4).getInt();
    }
    
    public int[] getIntArray(int elementCount) throws IOException {
	final int[] ary = new int[elementCount];
	map(elementCount*4).asIntBuffer().get(ary);
	return ary;
    }

    public short[] getShortArray(int elementCount) throws IOException {
	final short[] ary = new short[elementCount];
	map(elementCount*2).asShortBuffer().get(ary);
	return ary;
    }

    public char[] getCharArray(int elementCount) throws IOException {
	final char[] ary = new char[elementCount];
	map(elementCount*2).asCharBuffer().get(ary);
	return ary;
    }

    public String getString(int elementCount) throws IOException {
	return map(elementCount*2).asCharBuffer().toString();
    }

    public int size() throws IOException {
	return (int)cnl.size();
    }

    public void close() {
	try {
	    cnl.close();
	} catch (IOException e) {}
    }

    private ByteBuffer map(int size) throws IOException {
	cur += size;
	return cnl.map(FileChannel.MapMode.READ_ONLY, cur-size, size).order(ByteOrder.nativeOrder());
    }
}