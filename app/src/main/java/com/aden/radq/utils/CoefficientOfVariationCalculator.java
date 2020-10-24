package com.aden.radq.utils;

import java.util.List;

public class CoefficientOfVariationCalculator {

    public CoefficientOfVariationCalculator() {

    }

    public double calculate(List<Integer> heights){
        double sum = 0D;
        double summation = 0D;
        double standardDeviation;
        double average;
        double coefficientOfVariation;

        for(double height : heights){
            sum += height;
        }
        average = sum / heights.size();

        for(double height : heights){
            double aux = height - average;
            summation += aux * aux;
        }
        standardDeviation = Math.sqrt(summation/(heights.size()-1));

        //Coefficient of Variation
        // <15% homogeneous; 15% - 30%; 30%< heterogeneous
        coefficientOfVariation = (standardDeviation/average) * 100;
        return coefficientOfVariation;
    }
}
