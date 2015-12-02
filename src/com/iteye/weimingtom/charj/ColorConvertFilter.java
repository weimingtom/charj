package com.iteye.weimingtom.charj;
import java.awt.Color;

public class ColorConvertFilter extends AbstractFilter {
	public interface ColorReplace {
		void convert(int[] rgb);
	}

	private final ColorReplace colorReplace;
	private final float[] hsbOffsets;
	private final float grayLevel;
	private final int[][] gammaTbl;
	private final int[][] contrastTbl;
	
	public ColorConvertFilter(
			ColorReplace colorReplace,
			float[] hsbOffsets,
			float grayLevel,
			GammaTableFactory gammaTableFactory,
			ContrastTableFactory contrastTableFactory) {
		if (gammaTableFactory == null) {
			gammaTableFactory = new GammaTableFactory(1.f);
		}
		if (contrastTableFactory == null) {
			contrastTableFactory = new ContrastTableFactory(1.f);
		}
		if (hsbOffsets != null && hsbOffsets.length < 3) {
			throw new IllegalArgumentException("hsbOffset too short.");
		}
		if (hsbOffsets != null) {
			if (hsbOffsets[0] == 0 && hsbOffsets[1] == 0 && hsbOffsets[2] == 0) {
				hsbOffsets = null;
			} else {
				hsbOffsets = (float[]) hsbOffsets.clone();
			}
		}
		if (grayLevel < 0) {
			grayLevel = 0;
		} else if (grayLevel > 1) {
			grayLevel = 1.f;
		}
		this.grayLevel = grayLevel;
		this.gammaTbl = gammaTableFactory.createTable();
		this.contrastTbl = contrastTableFactory.createTable();
		this.hsbOffsets = hsbOffsets;
		this.colorReplace = colorReplace;
	}
	
	protected void filter(int[] pixcels) {
		final float grayLevel = this.grayLevel;
		final float negGrayLevel = 1.f - grayLevel;
		int[] precalc = new int[256];
		int[] negPrecalc = new int[256];
		for (int i = 0; i < 256; i++) {
			precalc[i] = (int)(i * grayLevel) & 0xff;
			negPrecalc[i] = (int)(i * negGrayLevel) & 0xff;
		}
		final ColorReplace colorReplace = this.colorReplace;
		int[] rgbvals = new int[3];
		final float[] hsbOffsets = this.hsbOffsets;
		final float[] hsbvals = new float[3];
		final int[][] gammaTbl = this.gammaTbl;
		final int mx = pixcels.length;
		for (int i = 0; i < mx; i++) {
			int argb = pixcels[i];
			int a = gammaTbl[0][(argb >> 24) & 0xff];
			int r = gammaTbl[1][(argb >> 16) & 0xff];
			int g = gammaTbl[2][(argb >> 8) & 0xff];
			int b = gammaTbl[3][(argb) & 0xff];
			if (colorReplace != null) {
				rgbvals[0] = r;
				rgbvals[1] = g;
				rgbvals[2] = b;
				colorReplace.convert(rgbvals);	
				r = rgbvals[0];
				g = rgbvals[1];
				b = rgbvals[2];
			}
			int br = ((77 * r + 150 * g + 29 * b) >> 8) & 0xff;
			r = ((int)(precalc[r] + negPrecalc[br])) & 0xff;
			g = ((int)(precalc[g] + negPrecalc[br])) & 0xff;
			b = ((int)(precalc[b] + negPrecalc[br])) & 0xff;
			if (hsbOffsets != null) {
				Color.RGBtoHSB(r, g, b, hsbvals);
				for (int l = 0; l < 3; l++) {
					hsbvals[l] += hsbOffsets[l];
				}
				for (int l = 1; l < 3; l++) {
					if (hsbvals[l] < 0) {
						hsbvals[l] = 0;
					} else if (hsbvals[l] > 1.f) {
						hsbvals[l] = 1.f;
					}
				}
				int rgb = Color.HSBtoRGB(hsbvals[0], hsbvals[1], hsbvals[2]);
				argb = (a << 24) | (rgb & 0xffffff);
			} else {
				argb = (a << 24) | (r << 16) | (g << 8) | b;
			}
			a = (argb >> 24) & 0xff;
			r = contrastTbl[0][(argb >> 16) & 0xff];
			g = contrastTbl[1][(argb >> 8) & 0xff];
			b = contrastTbl[2][(argb) & 0xff];
			argb = (a << 24) | (r << 16) | (g << 8) | b;

			pixcels[i] = argb;
		}
	}
}
