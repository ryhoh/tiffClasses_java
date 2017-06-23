// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

/**
 * Tiffファイルには読み込み用と書き込み用があるが、どちらにも共通する機能を
 * 抽象クラスで定義する
 */
package tiff;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class TiffFile {
	protected int imgHeight;			/* 画像の横幅 */
	protected int imgWidth;				/* 画像の縦幅 */
	protected long imgPos = -1;			/* 画像データの開始位置 */
	protected RandomAccessFile stream;	/* 入出力用ストリーム */
	protected long cursor = 0L;	// 読み込み位置を記憶
								// seekやread, writeをしたらその分進める
	
	public void close() throws IOException{
		this.stream.close();
	}
	
	public void setImgHeight(int x) {
		this.imgHeight = x;
	}
	
	public void setImgWidth(int x) {
		this.imgWidth = x;
	}
	
	public int getImgHeight() {
		return this.imgHeight;
	}
	
	public int getImgWidth() {
		return this.imgWidth;
	}
	
	public void setImgPos(long imgPos) {
		this.imgPos = imgPos;
	}
	
	public long getImgPos() {
		return this.imgPos;
	}
	
	public void setCursor(long cursor) {
		this.cursor = cursor;
	}
	
	public long getCursor() {
		return this.cursor;
	}
}
