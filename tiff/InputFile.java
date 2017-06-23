// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package tiff;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class InputFile extends TiffFile {
	
	public InputFile(String name) throws IOException {
		this.stream = new RandomAccessFile(name, "r");
	}
	
	// IDFヘッダを読んで必要なパラメータをセット
	public void setParams() throws IOException {
		int idfPos;
		int numOfEntry;
		
		cursor = 0L;		// 読み込み位置をリセット
		
		cursor += 4;		// IDFポインタへ
		this.stream.seek(cursor);
		idfPos = nextInt();
		
		cursor = 0L;		// 読み込み位置をリセット
		cursor += idfPos;	// IDFデータへ
		this.stream.seek(cursor);
		
		/*-------- IDFデータ --------*/
		numOfEntry = nextShort();	// エントリの数を確認
		
		for(int i = 0; i < numOfEntry; i++) {
			int header = nextShort();		// エントリタグ
			
			switch(header) {
			case 0x0100:		// 画像の横幅
				this.stream.seek(cursor += 6);
				this.imgWidth = nextInt();
				break;
			case 0x0101:		// 画像の縦幅
				this.stream.seek(cursor += 6);
				this.imgHeight = nextInt();
				break;
			case 0x0111:		// 画像データの開始位置
				this.stream.seek(cursor += 6);
				this.imgPos = nextInt();
				break;
			default:
				this.stream.seek(cursor += 10);	// 次のエントリタグまで進む
				break;
			}
		}
		/* 画像データの開始位置が存在するかチェック */
		if(this.imgPos == -1) {
			System.out.println("imgPos not found!");
			System.exit(1);
		}
	}
	
	// 位置cursorから読み込み、読み込んだ分cursorを進める
	// バイナリはリトルエンディアン、JavaVMはビッグエンディアン
	// バイトオーダを変換しながら読み書きしなければならない
	// https://teratail.com/questions/845
	// 1Byteはそのまま読んでも同じ
	public int nextInt() throws IOException {
		this.stream.seek(cursor);	// cursorが示す位置から読み取る
		
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(this.stream.readInt());
		buf.flip();
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		this.cursor += 4;	// 読んだ分カーソルを進める
		return buf.getInt();
	}
	
	public short nextShort() throws IOException {
		this.stream.seek(cursor);	// cursorが示す位置から読み取る
		
		ByteBuffer buf = ByteBuffer.allocate(2);
		buf.putShort(this.stream.readShort());
		buf.flip();
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		this.cursor += 2;	// 読んだ分カーソルを進める
		return buf.getShort();
	}
	
	// バイナリを1byte読んでbyte型にして返す
	// 戻り値：byte(-128から127)
	public byte nextByte() throws IOException {
		this.stream.seek(cursor);	// cursorが示す位置から読み取る
		
		// まず0から255の範囲で符号なし整数として読む
		//int data = this.stream.readUnsignedByte();
		
		this.cursor += 1;	// 読んだ分カーソルを進める
		
		// -128して、-128から127の範囲の整数にしてからbyteにキャストして返す
		//return (byte)(data - 128);
		return this.stream.readByte();
	}
	
	// 単純なコピー用に、バイトオーダを変えないで一気に読み込むメソッドを用意
	public int read(byte[] b, int off, int len) throws IOException {
		this.stream.seek(cursor);	// cursorが示す位置から読み取る
		
		int num = this.stream.read(b, off, len);
		cursor += num;	// num個読んだので、その分進める
		return num;	// 読み込んだ数を返す
	}
	/*public long readLong() throws IOException {
		this.stream.seek(cursor);	// cursorが示す位置から読み取る
		
		this.cursor += 8;
		return this.stream.readLong();
	}*/
	
}

/*public InputFile(String fileName) throws IOException{
//byte buff[];

this.stream = new DataInputthis.stream(new BufferedInputthis.stream(new FileInputthis.stream(fileName)));

fileSize = this.stream.available();	// これいる？
}*/
