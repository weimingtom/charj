package com.iteye.weimingtom.charj;
public enum ColorConv implements ColorConvertFilter.ColorReplace {
	NONE {
		@Override
		public void convert(int[] rgb) {
			// do nothing.
		}
	},
	BLUE {
		@Override
		public void convert(int[] rgb) {
			rgb[1] = rgb[0];
		}
	},
	VIOLET {
		@Override
		public void convert(int[] rgb) {
			rgb[1] = rgb[0];
			rgb[0] = rgb[2];
		}
	},
	RED {
		@Override
		public void convert(int[] rgb) {
			rgb[1] = rgb[0];
			rgb[0] = rgb[2];
			rgb[2] = rgb[1];
		}
	},
	YELLOW {
		@Override
		public void convert(int[] rgb) {
			rgb[1] = rgb[2];
			rgb[2] = rgb[0];
			rgb[0] = rgb[1];
		}
	},
	GREEN {
		@Override
		public void convert(int[] rgb) {
			rgb[1] = rgb[2];
			rgb[2] = rgb[0];
		}
	},
	CYAN {
		@Override
		public void convert(int[] rgb) {
			rgb[1] = rgb[2];
		}
	},
	BLACK {
		@Override
		public void convert(int[] rgb) {
			rgb[1] = rgb[0];
			rgb[2] = rgb[0];
		}
	},
	WHITE {
		@Override
		public void convert(int[] rgb) {
			rgb[0] = rgb[2];
			rgb[1] = rgb[2];
		}
	};
	
	public abstract void convert(int[] rgb);
}
