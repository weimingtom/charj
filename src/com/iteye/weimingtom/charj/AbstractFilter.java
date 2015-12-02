package com.iteye.weimingtom.charj;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

public abstract class AbstractFilter implements BufferedImageOp {
	protected abstract void filter(int[] pixcels);

	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
		if (destCM == null) {
			destCM = src.getColorModel();
		}
		int w = src.getWidth();
		int h = src.getHeight();
		return new BufferedImage(destCM,
				destCM.createCompatibleWritableRaster(w, h),
				destCM.isAlphaPremultiplied(), null);
	}

	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		if (dest == null) {
			dest = createCompatibleDestImage(src, null);
		}
		int w = src.getWidth();
		int h = src.getHeight();
		
		int imageType = src.getType();
		int [] pixcels;
		boolean shared = false;
		if (src == dest && (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB)) {
			pixcels = null;
			shared = true;
		} else {
			int len = w * h;
			pixcels = new int[len];
		}
		pixcels = getPixcels(src, 0, 0, w, h, pixcels);
		filter(pixcels);
		if (!shared) {
			setPixcels(dest, 0, 0, w, h, pixcels);
		}
		return dest;
	}
	

	public Rectangle2D getBounds2D(BufferedImage src) {
		int w = src.getWidth();
		int h = src.getHeight();
		return new Rectangle(0, 0, w, h);
	}

	public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
		return (Point2D) srcPt.clone();
	}

	public RenderingHints getRenderingHints() {
		return null;
	}

	protected int[] getPixcels(BufferedImage img, int x, int y, int w, int h, int[] pixcels) {
		if (w <= 0 || h <= 0) {
			return new int[0];
		}
		int len = w * h;
		if (pixcels != null && pixcels.length < len) {
			throw new IllegalArgumentException("array too short.");
		}
		int imageType = img.getType();
		if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB) {
			WritableRaster raster = img.getRaster();
			if (pixcels == null) {
				DataBufferInt buf = (DataBufferInt) raster.getDataBuffer();
				return buf.getData();
			}
			return (int[]) raster.getDataElements(x, y, w, h, pixcels);
		}
		if (pixcels == null) {
			throw new IllegalArgumentException("image type error.");
		}
		return img.getRGB(x, y, w, h, pixcels, 0, w);
	}

	protected void setPixcels(BufferedImage img, int x, int y, int w, int h, int[] pixcels) {
		int len = w * h;
		if (pixcels == null || w == 0 || h == 0) {
			return;
		}
		if (pixcels.length < len) {
			throw new IllegalArgumentException("array too short.");
		}
		int imageType = img.getType();
		if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB) {
			WritableRaster raster = img.getRaster();
			raster.setDataElements(x, y, w, h, pixcels);
			return;
		}
		img.setRGB(x, y, w, h, pixcels, 0, w);
	}
}
