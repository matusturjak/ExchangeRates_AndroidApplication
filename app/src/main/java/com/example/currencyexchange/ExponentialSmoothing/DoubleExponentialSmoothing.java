package com.example.currencyexchange.ExponentialSmoothing;

import com.example.currencyexchange.classes.LinearRegression;

/**
 * Trieda, ktora je zodpovedna za vypocet dvojiteho exponencialneho vyrovnavania
 */
public class DoubleExponentialSmoothing implements ExponentialSmoothing {
    private float[] modelData;
    private float b0;
    private float b1;
    private float st0;
    private float st1;
    private float mse;

    /**
     * Parametricky konstruktor triedy.
     * @param length
     * @param ahead
     */
    public DoubleExponentialSmoothing(int length, int ahead){
        this.modelData = new float[length + ahead];
        this.b0 = 0;
        this.b1 = 0;
        this.st0 = 0;
        this.st1 = 0;
        this.mse = 0;
    }

    /**
     * Metoda, ktora pocita predikcie dat vstupujucich ako parameter o jedno casove obdobie dopredu
     * pomocou dvojiteho exponencialneho vyrovnavania.
     * @param data
     * @param alpha
     */
    @Override
    public void fit(Float[] data, float alpha) {
        if(alpha > 1 || alpha <= 0) {
            throw new RuntimeException("parameter alpha je nekorektne zadany");
        }

        int[] time = new int[data.length];
        for(int i = 0;i < data.length; i++)
            time[i] = (i + 1);

        LinearRegression l = new LinearRegression();
        l.fit(time,data);
        this.b0 = (float)l.getIntercept();
        this.b1 = (float)l.getSlope();

        this.st0 = this.b0 - this.b1*((1 - alpha)/alpha);
        this.st1 = this.b0 - 2*((1-alpha)/alpha)*this.b1;
        this.modelData[0] = this.b0 + this.b1;

        for (int i = 0; i < data.length; i++){
            this.st0 = alpha*data[i] + (1-alpha)*this.st0;
            this.st1 = alpha*this.st0 + (1- alpha)*this.st1;

            this.b0 = 2*this.st0 - this.st1;
            this.b1 = ((alpha)/(1-alpha))*(this.st0 - this.st1);

            this.modelData[i+1] = this.b0 + this.b1;
        }

        for(int i=0; i < data.length; i++) {
            this.mse += Math.pow(data[i] - this.modelData[i],2);
        }
        this.mse = this.mse/data.length;
    }

    /**
     * Funkcia, ktora vypocita predikcie o n stanovenych casovych obdobi dopredu
     * pomocou dvojiteho exponencialneho vyrovnavania. Nasledne hodnoty predikcii vrati.
     * @param data
     * @param alpha
     * @return
     */
    @Override
    public float[] predict(Float[] data, float alpha) {
        this.fit(data,alpha);
        float[] predictions = new float[this.modelData.length - data.length];

        predictions[0] = this.modelData[data.length];
        for(int i = 0; i < this.modelData.length - data.length - 1; i++){
            this.st0 = alpha*this.modelData[data.length + i] + (1-alpha)*this.st0;
            this.st1 = alpha*this.st0 + (1- alpha)*this.st1;

            b0 = 2*this.st0 - this.st1;
            b1 = ((alpha)/(1-alpha))*(this.st0 - this.st1);

            this.modelData[data.length + i + 1] = b0 + b1;
            predictions[i+1] = this.modelData[data.length + i + 1];
        }

        return predictions;
    }

    /**
     * Vrati priemernu stvorcovu chybu predikcie.
     * @return
     */
    @Override
    public float getResiduals() {
        return this.mse;
    }
}
