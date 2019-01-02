package Math;

import java.util.Arrays;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class Polynomials {
	static public void test(int degree) {

		try {

			 double [] yRaw = new double [] {
		    		 43124470448.0,
		    		 47209839358.0,	
		    		 34161140501.0,
		    		 21229846866.0,
		    		 20334485855.0,
		    		 14675472924.0,
		    		 14673334280.0,
		    		 11630051224.0,
		    		 10049.0,
		    		 8004.0,
		    		 6391.0,
		    		 5389.0,
		    		 4322.0,
		    		 2303.0,
		    		 2618.0,
		    		 3026.0,
		    		 950.0,
		    		 86.0
		    };

		    double [] yDouble = new double [yRaw.length];
		    for(int i = 0; i < yRaw.length; i++)
		    	yDouble[i] = yRaw[yRaw.length - 1 - i];
		    double [] xDouble = new double [yRaw.length];
		    for(int i = 0; i < yRaw.length; i++)
		    	xDouble[i] = i;
		    
	        final WeightedObservedPoints obs = new WeightedObservedPoints();
	        for (int i = 0; i < yRaw.length; i++){
	            obs.add(xDouble[i], yDouble[i]);
	        }
	        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
	        final double[] coeff = fitter.fit(obs.toList());
	        System.out.println("coef="+Arrays.toString(coeff));
	        int idx = 10;
	        System.out.println("coef="+(coeff[0]*Math.pow(idx, 3) 
	        		+ coeff[1]*Math.pow(idx, 2)
	        		+ coeff[2]*Math.pow(idx, 1)
	        		+ coeff[3]*Math.pow(idx, 0)));
	        		
	        return;
		} catch (Exception e) {
			
		}
	}
}
