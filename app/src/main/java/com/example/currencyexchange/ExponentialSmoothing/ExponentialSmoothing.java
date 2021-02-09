package com.example.currencyexchange.ExponentialSmoothing;

public interface ExponentialSmoothing {
    void fit(Float[] data, float alpha);
    float[] predict(Float[] data, float alpha);
    float getResiduals();
}
