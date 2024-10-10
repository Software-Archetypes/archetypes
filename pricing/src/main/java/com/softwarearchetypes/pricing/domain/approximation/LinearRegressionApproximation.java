package com.softwarearchetypes.pricing.domain.approximation;

import com.softwarearchetypes.pricing.domain.PriceTable;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class LinearRegressionApproximation implements FormulaApproximation {

    private static final Logger log = LoggerFactory.getLogger(LinearRegressionApproximation.class);
    private static final String REGRESSION_FUNCTION = """
            {
              "+": [
                a1,
                { "*": [ b1, { "var": "x" } ] }
              ]
            }""";

    @Override
    public String approximate(PriceTable priceTable) {

        priceTable.getRows().forEach(row -> performLinearRegression(priceTable.getHeader(), row));

        return "";
    }

    private String performLinearRegression(List<BigDecimal> header, PriceTable.PriceTableRow row) {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();

        double[] xValues = header.stream().mapToDouble(BigDecimal::doubleValue).toArray();
        double[] yValues = row.getCosts().stream().mapToDouble(BigDecimal::doubleValue).toArray();

        //Transform headers to matrix (X)
        double[][] xMatrix = new double[yValues.length][1];
        for (int i = 0; i < yValues.length; i++) {
            xMatrix[i][0] = xValues[i];
        }

        regression.newSampleData(yValues, xMatrix);
        double[] coefficients = regression.estimateRegressionParameters();

        log.debug("For row with value Y = {}", row.getValue());
        log.debug("Regression function: y = {} + {} * x", coefficients[0], coefficients[1]);
        log.debug("R-squared: {}}", regression.calculateRSquared());

        return REGRESSION_FUNCTION
                .replace("a1", String.valueOf(coefficients[0]))
                .replace("b1", String.valueOf(coefficients[1]));
    }
}
