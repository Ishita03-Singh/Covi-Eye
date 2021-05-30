package com.example.healthmonitor.Math;

public class Vector {
    public static double[] normalize(double[] input) {
        double sumSquares = 0.0;

        for (int i = 0; i < input.length; i++) {
            sumSquares += Math.pow(input[i], 2);
        }

        double len = Math.sqrt(sumSquares);
        return Vector.scale(1 / len, input);
    }

    public static double[] invVector(
            double[] inVector) {

        int m = inVector.length;
        double[] outVector = new double[m];
        for (int i = 0; i < m; ++i) {
            if (inVector[i] != 0) {
                outVector[i] = 1 / inVector[i];
            } else {
                outVector[i] = 0;
            }
        }
        return (outVector);
    }


    public static boolean equals(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            return (false);
        }
        for (int i = 0; i < vec1.length; ++i) {
            if (vec1[i] != vec2[i]) {
                return (false);
            }
        }
        return (true);
    }

    private static String fillString(String in, int len) {
        String out = in;
        while (out.length() < len) {
            out = " " + out;
        }
        return (out);
    }

    public static String toString(double[] vector) {
        String result = "";
        for (int i = 0; i < vector.length; ++i) {
            result += fillString(Double.toString(vector[i]), 24) + "\n";
        }
        return (result);
    }


    public static double[] newVector(int m) {
        return (new double[m]);
    }


    public static double[] newVector(int m, double val) {
        double[] res = new double[m];
        for (int i = 0; i < m; ++i) {
            res[i] = val;
        }
        return (res);
    }


    public static double[] scale(double fac, double[] vector) {
        int n = vector.length;
        double[] res = new double[n];
        for (int i = 0; i < n; ++i) {
            res[i] = fac * vector[i];
        }
        return (res);
    }


    public static double dot(double[] vec1, double[] vec2) {
        int n = vec1.length;
        double res = 0.0;
        for (int i = 0; i < n; ++i) {
            res += vec1[i] * vec2[i];
        }
        return (res);
    }


    public static double[] add(double[] vec1, double[] vec2) {
        int m = vec1.length;
        double[] res = new double[m];
        for (int i = 0; i < m; ++i) {
            res[i] = vec1[i] + vec2[i];
        }
        return (res);
    }

    public static double[] sub(double[] vec1, double[] vec2) {
        int m = vec1.length;
        double[] res = new double[m];
        for (int i = 0; i < m; ++i) {
            res[i] = vec1[i] - vec2[i];
        }
        return (res);
    }


    public static double[] clone(double[] vector) {
        int m = vector.length;
        double[] res = new double[m];
        for (int i = 0; i < m; ++i) {
            res[i] = vector[i];
        }
        return (res);
    }


    public static double[] random(int m) {
        double[] res = new double[m];
        for (int i = 0; i < m; ++i) {
            res[i] = Math.random();
        }
        return (res);
    }


    public static double[][] addVecToSet(
            double[][] vecSet,
            double[] addVec) {
        int m = Matrix.getNumOfRows(vecSet);
        int n = Matrix.getNumOfColumns(vecSet);
        double[][] res = Matrix.newMatrix(m, n);
        for (int i = 0; i < m; ++i) {
            double add = addVec[i];
            for (int j = 0; j < n; ++j) {
                res[i][j] = vecSet[i][j] + add;
            }
        }
        return (res);
    }

    public static double[] center(double[] vec) {
        int n = vec.length;
        double mValue = 0.0;
        for (int i = 0; i < n; i++) {
            mValue += vec[i];
        }
        mValue /= n;

        double[] cVec = new double[n];
        for (int i = 0; i < n; i++) {
            cVec[i] = vec[i] - mValue;
        }
        return cVec;
    }
}
