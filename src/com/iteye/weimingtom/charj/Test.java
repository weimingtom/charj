package com.iteye.weimingtom.charj;
import java.io.IOException;
import java.awt.Color;

public class Test {
	public static void main(String[] args) throws IOException {
		new ImageResource("preview.png")
			.load()
			//.drawAlpha()
			//.rescale(1.5f, 0.0f)
			.colorConv()
			.save("out.jpg", Color.blue);
		System.out.println("over");
	}
}
