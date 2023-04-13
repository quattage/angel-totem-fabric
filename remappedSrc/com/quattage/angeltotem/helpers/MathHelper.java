package com.quattage.angeltotem.helpers;

public class MathHelper {
    public static float clampValue(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}
    
    public static float randomF(float minNumber, float maxNumber) {
        return minNumber + (float)(Math.random() * ((maxNumber - minNumber) + 1));
    }

    public static double randomD(double minNumber, double maxNumber) {
        return minNumber + (Math.random() * ((maxNumber - minNumber) + 1));
    }

    public static int randomI(int minNumber, int maxNumber) {
        return minNumber + (int)(Math.random() * ((maxNumber - minNumber) + 1));
    }
}
