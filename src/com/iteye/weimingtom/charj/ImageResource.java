package com.iteye.weimingtom.charj;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * @see http://sourceforge.jp/projects/charactermanaj/
 * @author Administrator
 *
 */
public class ImageResource {
	private File file;
	private BufferedImage img;
	
	public ImageResource(String filename) {
		this(new File(filename));
	}
	
	public ImageResource(File file) {
		if (file == null) {
			throw new IllegalArgumentException();
		}
		this.file = file;
	}
	
	private InputStream openStream() throws IOException {
		return new BufferedInputStream(new FileInputStream(file));
	}
	
	@Override
	public String toString() {
		return file.toString();
	}
	
	public ImageResource load() throws IOException {
		InputStream is = this.openStream();
		try {
			img = ImageIO.read(is);
		} finally {
			is.close();
		}
		if (img == null) {
			throw new IOException("unsupported image");
		}
		img = convertARGB(img);
		return this;
	}
	
	private BufferedImage convertARGB(BufferedImage image) {
		if (image == null) {
			throw new IllegalArgumentException();
		}
		int typ = image.getType();
		if (typ == BufferedImage.TYPE_INT_ARGB) {
			return image;
		}
		BufferedImage img2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = img2.getGraphics();
		try {
			g.drawImage(image, 0, 0, null);
		} finally {
			g.dispose();
		}
		return img2;
	}
	
	
	public ImageResource save(String outFileName) throws IOException {
		this.save(outFileName, null);
		return this;
	}
	
	public ImageResource save(String outFileName, Color imgBgColor) throws IOException {
		this.save(new File(outFileName), imgBgColor);
		return this;
	}
	
	public ImageResource save(File outFile, Color imgBgColor) throws IOException {
		if (img == null || outFile == null) {
			throw new IllegalArgumentException();
		}
		String fname = outFile.getName();
		int extpos = fname.lastIndexOf(".");
		if (extpos < 0) {
			throw new IOException("missing file extension.");
		}
		String ext = fname.substring(extpos + 1).toLowerCase();
		Iterator<ImageWriter> ite = ImageIO.getImageWritersBySuffix(ext);
		if (!ite.hasNext()) {
			throw new IOException("unsupported file extension: " + ext);
		}
		ImageWriter iw = ite.next();
		save(iw, outFile, imgBgColor);
		return this;
	}
	
	public ImageResource save(OutputStream outstm, String mime, Color imgBgColor) throws IOException {
		if (img == null || outstm == null || mime == null) {
			throw new IllegalArgumentException();
		}
		int pt = mime.indexOf(';');
		if (pt >= 0) {
			mime = mime.substring(0, pt).trim();
		}
		Iterator<ImageWriter> ite = ImageIO.getImageWritersByMIMEType(mime);
		if (!ite.hasNext()) {
			throw new IOException("unsupported mime: " + mime);
		}
		ImageWriter iw = ite.next();
		save(iw, outstm, imgBgColor);
		outstm.flush();
		return this;
	}
	
	private void save(ImageWriter iw, Object output, Color imgBgColor) throws IOException {
		try {
			boolean jpeg = false;
			boolean bmp = false;
			for (String mime : iw.getOriginatingProvider().getMIMETypes()) {
				if (mime.contains("image/jpeg") || mime.contains("image/jpg")) {
					jpeg = true;
					break;
				}
				if (mime.contains("image/bmp") || mime.contains("image/x-bmp")
						|| mime.contains("image/x-windows-bmp")) {
					bmp = true;
					break;
				}
			}
			ImageWriteParam iwp = iw.getDefaultWriteParam();
			IIOImage ioimg;
			if (jpeg) {
				iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				iwp.setCompressionQuality(getJpegQuality());
				ioimg = new IIOImage(createFormatPicture(imgBgColor, BufferedImage.TYPE_INT_BGR), null, null);
			} else if (bmp) {
				ioimg = new IIOImage(createFormatPicture(imgBgColor, BufferedImage.TYPE_3BYTE_BGR), null, null);
			} else if (isForceBgColor()) {
				ioimg = new IIOImage(createFormatPicture(imgBgColor, BufferedImage.TYPE_INT_ARGB), null, null);
			} else {
				ioimg = new IIOImage(img, null, null);
			}
			ImageOutputStream imgstm = ImageIO.createImageOutputStream(output);
			try {
				iw.setOutput(imgstm);
				iw.write(null, ioimg, iwp);
			} finally {
				imgstm.close();
			}
		} finally {
			iw.dispose();
		}
	}

	private BufferedImage createFormatPicture(Color imgBgColor, int type) {
		if (img == null) {
			throw new IllegalArgumentException();
		}
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage tmpImg = new BufferedImage(w, h, type);
		Graphics2D g = tmpImg.createGraphics();
		try {
			g.setRenderingHint(
					RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			if (imgBgColor == null) {
				imgBgColor = Color.WHITE;
			}
			if (imgBgColor != null) {
				g.setColor(imgBgColor);
				g.fillRect(0, 0, w, h);
			}
			g.drawImage(img, 0, 0, w, h, 0, 0, w, h, null);
		} finally {
			g.dispose();
		}
		return tmpImg;
	}
	
	//FIXME:
	private boolean isForceBgColor() {
		return false;
	}
	
	//FIXME:
	private float getJpegQuality() {
		return 1.0f;
	}
	
	/**
	 * 原图与bgColor混合，基于像素的alpha，然后设置alpha为255
	 * @param bgColor
	 * @return
	 */
	public ImageResource alphabrend(Color bgColor) {
		BackgroundColorFilter bgColorFilter = new BackgroundColorFilter(
				BackgroundColorFilter.BackgroundColorMode.ALPHABREND, bgColor);
			img = bgColorFilter.filter(img, null);
			return this;
	}
	
	/**
	 * 把所有alpha为0的像素替换为bgColor
	 * @param bgColor
	 * @return
	 */
	public ImageResource opaque(Color bgColor) {
		BackgroundColorFilter bgColorFilter = new BackgroundColorFilter(
			BackgroundColorFilter.BackgroundColorMode.OPAQUE, bgColor);
		img = bgColorFilter.filter(img, null);
		return this;
	}
	
	/**
	 * 灰度化，红绿蓝的平均值乘于alpha为灰度值
	 * @return
	 */
	public ImageResource grayscale() {
		BackgroundColorFilter bgColorFilter = new BackgroundColorFilter(
			BackgroundColorFilter.BackgroundColorMode.GRAYSCALE, null);
		img = bgColorFilter.filter(img, null);
		return this;
	}
	
	/**
	 * 红绿蓝分别是灰度，是否全透明，透明度
	 * 应该是测试用
	 * @return
	 */
	public ImageResource drawAlpha() {
		BackgroundColorFilter bgColorFilter = new BackgroundColorFilter(
			BackgroundColorFilter.BackgroundColorMode.DRAW_ALPHA, null);
		img = bgColorFilter.filter(img, null);
		return this;
	}
	
	/*
	null || ColorConv.VIOLET
	hsbOffsets 0.0, 0.0, 0.0, 0.0
	grayLevel 1.0
	gamma 1.0 1.0 1.0 1.0
	contrastTableFactory 1.0
	*/
	public ImageResource colorConv() {
		float[] hsbOffsets = new float[] {0.0f, 0.0f, 0.0f, 0.0f};  
		float[] gamma = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
		ColorConvertFilter colorConvertFilter = new ColorConvertFilter(
				ColorConv.GREEN, hsbOffsets, 1.0f, new GammaTableFactory(1.0f), new ContrastTableFactory());
			img = colorConvertFilter.filter(img, null);
		return this;
	}
	
	/**
	 * rgb = (rgb * factors) + offsets
	 * @return
	 */
	public ImageResource rescale(float factor, float offset) {
		float[] factors = new float[]{factor, factor, factor, factor};
		float[] offsets = new float[]{offset, offset, offset, offset};
		RescaleOp rescale_op = new RescaleOp(factors, offsets, null);
		img = rescale_op.filter(img, img);
		return this;
	}
}
