package fr.arolla.util;

import java.util.List;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Sampler {

    public double[] sample(int sampling, List<Double> values) {
        int nb = (int) Math.ceil(d(values.size()) / d(sampling));
        double[] extrated = new double[nb];
        int pos = 0;
        for (int i = 0; i < values.size(); i++) {
            if (i % sampling == 0) {
                extrated[pos++] = values.get(i);
            }
        }
        return extrated;
    }


    private static double d(int x) {
        return x;
    }
}
