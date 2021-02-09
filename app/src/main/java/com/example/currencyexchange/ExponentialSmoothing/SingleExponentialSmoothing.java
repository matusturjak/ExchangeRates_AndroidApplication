package com.example.currencyexchange.ExponentialSmoothing;

/**
 * Trieda, ktora sa stara o vypocet predikcii dat pomocou jednoducheho exponencialneho vyrovnavania
 */
public class SingleExponentialSmoothing implements ExponentialSmoothing {
    private float[] modelData;
    private float st0;
    private float mse;

    /**
     * Parametricky konstruktor triedy
     * @param length
     * @param ahead
     */
    public SingleExponentialSmoothing(int length, int ahead){
        this.modelData = new float[length + ahead];
        this.st0 = 0;
        this.mse = 0;
    }

    /**
     * Metoda, ktora pocita predikcie dat vstupujucich ako parameter o jedno casove obdobie dopredu
     * pomocou jednoducheho exponencialneho vyrovnavania.
     * @param data
     * @param alpha
     */
    @Override
    public void fit(Float[] data, float alpha) {
        if(alpha > 1 || alpha <= 0) {
            throw new RuntimeException("parameter alpha je nekorektne zadany");
        }

        float sum = 0;
        for(int i = 0; i < data.length; i++){
            sum += data[i];
        }

        this.st0 = sum/data.length;
        this.modelData[0] = sum/data.length;

        for(int i = 0; i < data.length; i++){
            this.modelData[i+1] = alpha*data[i] + (1 - alpha)*this.st0;
            this.st0 = this.modelData[i+1];
        }

        for(int i=0; i < data.length; i++) {
            this.mse += Math.pow(data[i] - this.modelData[i],2);
        }
        this.mse = this.mse/data.length;

    }

    /**
     * Funkcia, ktora vypocita predikcie o n stanovenych casovych obdobi dopredu
     * pomocou jednoducheho exponencialneho vyrovnavania. Nasledne hodnoty predikcii vrati.
     * @param data
     * @param alpha
     * @return
     */
    @Override
    public float[] predict(Float[] data, float alpha) {
        this.fit(data, alpha);
        float[] predictions = new float[this.modelData.length - data.length];
        predictions[0] = this.modelData[data.length];
        for(int i = 0;i < this.modelData.length - data.length - 1; i++){
            this.modelData[data.length + i + 1] = alpha*this.modelData[data.length + i] + (1 - alpha)*this.st0;
            this.st0 = this.modelData[data.length + i + 1];
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
