package com.example.currencyexchange.classes;

/**
 * Trieda, ktora sa stara o najdenie optimalnych parametrov regresneho modelu
 */
public class LinearRegression {
    private double intercept, slope;

    /**
     * Metoda, ktora urci najlepsie odhady parametrov regresneho modelu vytvoreneho z dat y
     * @param x
     * @param y
     */
    public void fit(int[] x, Float[] y){
        if (x.length != y.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        int n = x.length;

        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < n; i++) {
            sumx  += x[i];
            sumx2 += x[i]*x[i];
            sumy  += y[i];
        }
        double xbar = sumx / n;
        double ybar = sumy / n;


        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        slope  = xybar / xxbar;
        intercept = ybar - slope * xbar;
    }

    /**
     * Vrati trendovy parameter modelu.
     * @return
     */
    public double getSlope() {
        return slope;
    }

    /**
     * Vrati konstantny parameter modelu.
     * @return
     */
    public double getIntercept() {
        return intercept;
    }
}
