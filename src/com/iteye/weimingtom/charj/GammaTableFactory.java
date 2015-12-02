package com.iteye.weimingtom.charj;

public class GammaTableFactory implements TableFactory {
	private float[] gammas;
	
	public GammaTableFactory(float gamma) {
		setGamma(gamma);
	}
	
	public GammaTableFactory(float[] gammas) {
		setGamma(gammas);
	}

	public final void setGamma(float gamma) {
		setGamma(new float[] {gamma, gamma, gamma, gamma});
	}
	
	public final void setGamma(float[] gammas) {
		if (gammas == null || gammas.length < 3) {
			throw new IllegalArgumentException();
		}
		this.gammas = gammas;
	}
	
	public int[][] createTable() {
		int mx = gammas.length;
		int[][] gammaTbls = new int[mx][];
		for (int i = 0; i < 4; i++) {
			float gamma;
			if (i < mx) {
				gamma = gammas[i];
			} else {
				gamma = 1.f;
			}
			gammaTbls[i] = createGamma(gamma);
		}
		return gammaTbls;
	}
	
	private int[] createGamma(float gamma) {
		if (gamma < 0.01f) {
			gamma = 0.01f;
		}
		int gammaTbl[] = new int[256];
		for (int gi = 0; gi <= 0xff; gi++) {
			gammaTbl[gi] = (int)(Math.pow(gi / 255.0, 1 / gamma) * 255) & 0xff;
		}
		return gammaTbl;
	}
}
