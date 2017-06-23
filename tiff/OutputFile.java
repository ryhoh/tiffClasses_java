// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package tiff;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class OutputFile extends TiffFile {
	
	public OutputFile(String name) throws FileNotFoundException {
		this.stream = new RandomAccessFile(name, "rw");
	}

	// 書き込みメソッドでは、書き込み後に書き込んだ分だけcursorを移動させる
	
	public void writeByte(byte x) throws IOException {
		this.stream.seek(cursor);	// cursorが示す位置から書き込む
		
		this.stream.write(x);
		this.cursor += 1;
	}
	
	// 配列から全て書き込む
	public void write(byte[] buffer) throws IOException {
		this.stream.seek(cursor);	// cursorが示す位置から書き込む
		
		this.stream.write(buffer);
		this.cursor += buffer.length;
	}
	
	// 配列からlength個書き込む
	public void write(byte[] buffer, int offset, int length) throws IOException {
		this.stream.seek(cursor);	// cursorが示す位置から書き込む
		
		this.stream.write(buffer, offset, length);
		this.cursor += length;
	}
	
	// 単純なコピー用に、バイトオーダを変えないで一気に書き込むメソッドを用意
		// 上のメソッドを流用
	
	
	/*public void writeLong(long buffer) throws IOException {
		this.stream.seek(cursor);	// cursorが示す位置から書き込む
		
		this.stream.writeLong(buffer);
		this.stream.seek(cursor += 8);
	}*/
}
