// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package tiff;

import java.io.EOFException;
import java.io.IOException;

import progressBar.ProgressBar;

// 比較明合成処理のピクセル比較部分の手続きを実現するインタフェース

public interface Composite extends ProgressBar {
	
	// 全てのデータをコピー
	public static void copyAll(InputFile inputFile, OutputFile outputFile) throws IOException {
		
		// カーソルの位置を最初に合わせる
		inputFile.setCursor(0L);
		outputFile.setCursor(0L);
		
		// ループ制御用に書き込むファイルサイズを確認
		final long FILESIZE = inputFile.stream.length();
		
		final int SIZE = 65536;
		byte[] buffer = new byte[SIZE];	// コピーするデータを一時的に持つ
		
		for(int i = 0; i < FILESIZE / SIZE; i++) {
			inputFile.read(buffer, 0, SIZE);
			outputFile.write(buffer, 0, SIZE);
		}
		int loaded = inputFile.read(buffer, 0, SIZE);	// 読み込んだ数
		outputFile.write(buffer, 0, loaded);			// だけ書き込む
		
		/*
		for(int i = 0; i < FILESIZE / 8 + 1; i++) {
			outputFile.writeLong(inputFile.readLong());
		}*/
	}
	
	// 全ての入力ファイルの縦横の長さが同一であることを確かめる
	public static void checkLength(InputFile[] inputFiles) {
		
		final int HEIGHT = inputFiles[0].getImgHeight();
		final int WIDTH = inputFiles[0].getImgWidth();
		
		for(InputFile inputFile : inputFiles) {
			if(HEIGHT != inputFile.getImgHeight() || WIDTH != inputFile.getImgWidth()) {
				System.out.println("invalid image length");
				System.exit(1);
			}
		}
	}
	
	// 一度に全ての画素を配列に入れるのは不可能
	// 画像を先頭からJOB画素ずつに分割して、それぞれについて処理し、逐次書き込む
	public static void dividePixels(InputFile[] inputFiles, OutputFile outputFile) throws IOException {
		
		// 総画素数を計算
		final int IMGSIZE = inputFiles[0].getImgHeight() * inputFiles[0].getImgWidth();
		
		final int JOB_PIXEL = 5000;
		
		final int JOB_BYTES = JOB_PIXEL * 3;				// 1回に処理するバイト数
		final int EXTRAJOB_BYTES = (IMGSIZE % JOB_PIXEL);	// 余ったバイト数だけ最後に追加処理
		final int NUMOFPHAZE = IMGSIZE / JOB_PIXEL;			// 処理の回数
		
		// 作業開始前に1度プログレスバーを見せる
		ProgressBar.printProgress(0, NUMOFPHAZE);
		
		// 全てのファイルのカーソルを画像データ開始位置へ
		for(InputFile inputFile : inputFiles) {
			inputFile.setCursor(inputFile.getImgPos());
		}
		outputFile.setCursor(outputFile.getImgPos());
		
		/*--------------- 処理 ---------------*/
		byte[] array = new byte[JOB_BYTES];			// ここに比較結果が格納される
		byte[] inputPixels = new byte[JOB_BYTES];	// ここに入力データが入る
		for(int i = 0; i < NUMOFPHAZE; i++) {
			clear(array);	// 配列をbyte型の最小値(-128)で埋める
			
			// 比較
			for(InputFile inputFile : inputFiles) {
				inputFile.read(inputPixels, 0, JOB_BYTES);
				compare(array, inputPixels, JOB_BYTES);
			}
			
			// 書き込み
			outputFile.write(array);
			/*for(byte by : array) {
				outputFile.writeByte(by);
			}*/
			
			// 途中経過を示す
			if(i % 10 == 0) {
				ProgressBar.printProgress(i, NUMOFPHAZE);
			}
		}
		/*---------- 残った端切れ部分の処理 ----------*/
		clear(array);
		
		// 比較
		try {
			for(InputFile inputFile : inputFiles) {
				inputFile.read(inputPixels, 0, EXTRAJOB_BYTES);
				compare(array, inputPixels, EXTRAJOB_BYTES);
			}
		} catch (EOFException e) {
			// 正常動作でEOFになることが想定されるので何もしない
		}
		
		// 書き込み
		outputFile.write(array, 0, EXTRAJOB_BYTES);
		/*for(int i = 0; i < EXTRAJOB_BYTES; i++) {
			outputFile.writeByte(array[i]);
		}*/
		
		/* ---------------------------------------- */
		
		// 実行完了を示す
		ProgressBar.printProgress(NUMOFPHAZE, NUMOFPHAZE);
		
	}
	
	// 今までの輝度のうち最も明るい数値を記録した配列arrayと、inputFileの数値を比較して、輝度の高い数値を配列に入れる
	// 初回の比較では必ず全てのデータが配列に書き込まれることになる
	// 必ずしも配列の全てを使う訳ではないので、処理するbyte数numを与える
	public static void compare(byte[] base, byte[] pixels, int num) throws IOException {
		
		for(int i = 0; i < num / 3; i++) {
			
			if(culcBrightness(base[i*3], base[i*3 + 1], base[i*3 + 2]) < culcBrightness(pixels[i*3], pixels[i*3 + 1], pixels[i*3 + 2])) {
				// ピクセルを更新
				for(int j = 0; j < 3; j++) {
					base[i*3 + j] = pixels[i*3 + j];
				}
			}
			
			/*if(culcBrightness(pixels[i*3], pixels[i*3 + 1], pixels[i*3 + 2]) < 0) {
				System.out.println((long)pixels[i*3] + " " + (long)pixels[i*3 + 1] + " " + (long)pixels[i*3 + 2]);
				System.out.println(culcBrightness(pixels[i*3], pixels[i*3 + 1], pixels[i*3 + 2]));
			}*/
		}
		
	}
	
	// rgbのそれぞれの輝度が1byteで表され渡される
	// バイナリでは0から255で表されるが、Javaの仕様ではbyte型は-128から127で渡される
	// 128を加えて符号なし数として扱い計算する
	static int culcBrightness(byte rb, byte gb, byte bb) {
		
		// byte型では最上位ビットは符号として処理されるが、実データは符号なし整数である
		// 本来の数をintで表す
		int r = 0xFF & rb;
		int g = 0xFF & gb;
		int b = 0xFF & bb;
		
		// pchansblog.exblog.jp/26051068/
		return 306 * r + 601 * g + 117 * b;
		// 正しい輝度としては最後に512を足すが、比較目的なので省略
	}
	
	static void clear(byte[] array) {
		// byte型の最小値で初期化
		// 全てのビットを0にする
		for(int i = 0; i < array.length; i++) {
			array[i] = 0;	// 0b00000000 にする
		}
	}
	
}
