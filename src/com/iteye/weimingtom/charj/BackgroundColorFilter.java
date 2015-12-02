package com.iteye.weimingtom.charj;
import java.awt.Color;

public class BackgroundColorFilter extends AbstractFilter {
	public enum BackgroundColorMode {
		ALPHABREND(false, false) {
			@Override
			public void filter(BackgroundColorFilter me, int[] pixcels) {
				me.alphabrend(pixcels);
			}
		},
		OPAQUE(false, true) {
			@Override
			public void filter(BackgroundColorFilter me, int[] pixcels) {
				me.opaque(pixcels);
			}
		},
		GRAYSCALE(true, false) {
			@Override
			public void filter(BackgroundColorFilter me, int[] pixcels) {
				me.grayscale(pixcels);
			}
		},
		DRAW_ALPHA(true, true) {
			@Override
			public void filter(BackgroundColorFilter me, int[] pixcels) {
				me.drawAlpha(pixcels);
			}
		};
		
		private final boolean grayscale;
		private final boolean noAlphachanel;
		public abstract void filter(BackgroundColorFilter me, int[] pixcels);
		
		BackgroundColorMode(boolean grayscale, boolean noAlphachanel) {
			this.grayscale = grayscale;
			this.noAlphachanel = noAlphachanel;
		}
		
		public boolean isNoAlphaChannel() {
			return this.noAlphachanel;
		}
		
		public boolean isGrayscale() {
			return this.grayscale;
		}
		
		public static BackgroundColorMode valueOf(boolean noAlphachanel, boolean grayscale) {
			for (BackgroundColorMode mode : values()) {
				if (mode.isNoAlphaChannel() == noAlphachanel && mode.isGrayscale() == grayscale) {
					return mode;
				}
			}
			throw new RuntimeException("mode error");
		}
		
	}
	
	private BackgroundColorMode mode;
	private Color bgColor;

	public BackgroundColorFilter(BackgroundColorMode mode, Color bgColor) {
		if (mode == null) {
			throw new IllegalArgumentException();
		}
		if (!mode.isGrayscale() && bgColor == null) {
			throw new IllegalArgumentException();
		}
		this.mode = mode;
		this.bgColor = bgColor;
	}
	
	@Override
	protected void filter(int[] pixcels) {
		mode.filter(this, pixcels);
	}
	
	public void alphabrend(int[] pixcels) {
		int br = bgColor.getRed();
		int bg = bgColor.getGreen();
		int bb = bgColor.getBlue();
		final int mx = pixcels.length;
		for (int idx = 0; idx < mx; idx++) {
			int argb = pixcels[idx];
			int b = argb & 0xff;
			int g = (argb >>>= 8) & 0xff;
			int r = (argb >>>= 8) & 0xff;
			int a = (argb >>>= 8) & 0xff;
			if (a == 0) {
				b = bb;
				g = bg;
				r = br;
			} else if (a != 0xff) {
				b = ((b * a) / 0xff + (bb * (0xff - a) / 0xff)) & 0xff;
				g = ((g * a) / 0xff + (bg * (0xff - a) / 0xff)) & 0xff;
				r = ((r * a) / 0xff + (br * (0xff - a) / 0xff)) & 0xff;
			}
			argb = 0xff000000 | (r << 16) | (g << 8) | b;
			pixcels[idx] = argb;
		}
	}

	public void opaque(int[] pixcels) {
		int bgRgb = bgColor.getRGB();
		final int mx = pixcels.length;
		for (int idx = 0; idx < mx; idx++) {
			int argb = pixcels[idx];
			int a = (argb >>> 24) & 0xff;
			int rgb = (argb & 0xffffff);
			if (a == 0) {
				rgb = bgRgb;
			}
			argb = 0xff000000 | rgb;
			pixcels[idx] = argb;
		}
	}
	
	public void grayscale(int[] pixcels) {
		final int mx = pixcels.length;
		for (int idx = 0; idx < mx; idx++) {
			int argb = pixcels[idx];
			int b = argb & 0xff;
			int g = (argb >>>= 8) & 0xff;
			int r = (argb >>>= 8) & 0xff;
			int a = (argb >>>= 8) & 0xff;
			int gray_brend = 0;
			int gray_plain = 0;
			if (a != 0) {
				gray_brend = ((r + g + b) / 3) & 0xff;
				gray_plain = gray_brend;
				if (a != 0xff) {
					gray_brend = ((gray_brend * a) / 0xff) & 0xff;
				}
			}			
			argb = 0xff000000 | (gray_brend << 16) | (gray_plain << 8) | gray_brend;
			pixcels[idx] = argb;
		}
	}

	public void drawAlpha(int[] pixcels) {
		final int mx = pixcels.length;
		for (int idx = 0; idx < mx; idx++) {
			int argb = pixcels[idx];
			int b = argb & 0xff;
			int g = (argb >>>= 8) & 0xff;
			int r = (argb >>>= 8) & 0xff;
			int a = (argb >>>= 8) & 0xff;
			int gray_plain = (r + g + b) / 3;
			int alpha_off = (a == 0) ? 0x80 : (a == 0xff) ? 0x00 : 0xff;
			argb = 0xff000000 | (gray_plain << 16) | (alpha_off << 8) | a;
			pixcels[idx] = argb;
		}
	}
}
