package com.softwarearchetypes.pricing.domain.approximation;

import com.softwarearchetypes.pricing.domain.PriceTable;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.util.Pair;

import java.math.BigDecimal;
import java.util.List;

public class NonlinearRegressionApproximation implements FormulaApproximation {

    private static final String FORMULA_TEMPLATE = """
            {
              "+": [
                { "*": [ "a", { "^": [ { "var": "x" }, "b" ] } ] },
                { "*": [ "c", { "^": [ { "var": "y" }, "d" ] } ] }
              ]
            }
            """;


    @Override
    public String approximate(PriceTable priceTable) {

        // Przygotowanie danych do regresji nieliniowej
        RegressionData regressionData = prepareRegressionData(priceTable);

        // Parametry początkowe (a, b, c, d)
        double[] initialGuess = {1, 1, 1, 1};

        // Tworzenie i wykonywanie regresji nieliniowej
        double[] coefficients = performNonlinearRegression(regressionData.X(), regressionData.Y(), initialGuess);

        return FORMULA_TEMPLATE
                .replace("a", String.valueOf(coefficients[0]))
                .replace("b", String.valueOf(coefficients[1]))
                .replace("c", String.valueOf(coefficients[2]))
                .replace("d", String.valueOf(coefficients[3]))
                ;
    }

    // Metoda do wykonania regresji nieliniowej
    private double[] performNonlinearRegression(double[][] x, double[] y, double[] initialGuess) {
        // Definicja funkcji nieliniowej y = a * x1^b + c * x2^d
        MultivariateJacobianFunction model = point -> {
            double[] values = new double[y.length];
            double[][] jacobian = new double[y.length][initialGuess.length];

            for (int i = 0; i < point.toArray().length; i++) {
                double x1 = x[i][0]; // X1 (nagłówki)
                double x2 = x[i][1]; // X2 (wartości wierszy)
                double a = point.toArray()[0];
                double b = point.toArray()[1];
                double c = point.toArray()[2];
                double d = point.toArray()[3];

                // Funkcja: y = a * x1^b + c * x2^d
                values[i] = a * Math.pow(x1, b) + c * Math.pow(x2, d);

                // Różniczki cząstkowe do jacobiana
                jacobian[i][0] = Math.pow(x1, b); // ∂y/∂a
                jacobian[i][1] = a * Math.pow(x1, b) * Math.log(x1); // ∂y/∂b
                jacobian[i][2] = Math.pow(x2, d); // ∂y/∂c
                jacobian[i][3] = c * Math.pow(x2, d) * Math.log(x2); // ∂y/∂d
            }

            return new Pair<>(new ArrayRealVector(values), new Array2DRowRealMatrix(jacobian));
        };


        // Użycie optymalizatora Levenberg-Marquardt
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        // Konfiguracja problemu najmniejszych kwadratów
        LeastSquaresProblem problem = new LeastSquaresBuilder()
                .start(initialGuess)
                .model(model)
                .target(y)
                .checkerPair(new SimpleVectorValueChecker(1e-10, 1e-10))
                .lazyEvaluation(false)
                .maxEvaluations(1000)
                .maxIterations(1000)
                .build();

        // Znalezienie optymalnych współczynników
        return optimizer.optimize(problem).getPoint().toArray();
    }

    // Klasa pomocnicza do przechowywania danych do regresji
    private RegressionData prepareRegressionData(PriceTable priceTable) {
        List<BigDecimal> headers = priceTable.getHeader();
        List<PriceTable.PriceTableRow> rows = priceTable.getRows();

        int numRows = rows.size();
        int numFeatures = 2; // Zmienna 1: X1 (nagłówki), Zmienna 2: X2 (wiersze)

        double[][] X = new double[numRows * headers.size()][numFeatures];
        double[] Y = new double[numRows * headers.size()];

        int index = 0;
        for (int i = 0; i < numRows; i++) {
            PriceTable.PriceTableRow row = rows.get(i);
            BigDecimal rowValue = row.getValue();
            List<BigDecimal> costs = row.getCosts();

            for (int j = 0; j < headers.size(); j++) {
                BigDecimal headerValue = headers.get(j);
                BigDecimal cost = costs.get(j);

                X[index][0] = headerValue.doubleValue(); // Zmienna X1
                X[index][1] = rowValue.doubleValue(); // Zmienna X2
                Y[index] = cost.doubleValue(); // Zmienna Y
                index++;
            }
        }

        return new RegressionData(X, Y);
    }


    // Klasa pomocnicza do przechowywania danych
    private static record RegressionData(
            double[][] X,
            double[] Y
    ) {
    }
}
