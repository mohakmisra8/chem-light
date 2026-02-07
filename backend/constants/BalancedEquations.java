package backend.constants;

public class BalancedEquations extends Equations {
    private double[] coefficients;
    
    public BalancedEquations(Input[] inputs, Output[] outputs, double[] coefficients) {
        super(inputs, outputs);
        this.coefficients = coefficients;
    }
    
    public double[] getCoefficients() {
        return coefficients;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Build input string with coefficients: "coeff1 Input A + coeff2 Input B + ..."
        Input[] inputs = getInputs();
        for (int i = 0; i < inputs.length; i++) {
            int coeff = (int) Math.round(coefficients[i]);
            if (coeff != 1) {
                sb.append(coeff).append(" ");
            }
            sb.append(inputs[i].toString());
            if (i < inputs.length - 1) {
                sb.append(" + ");
            }
        }
        
        // Add arrow separator
        sb.append(" -> ");
        
        // Build output string with coefficients: "coeff1 Output A + coeff2 Output B + ..."
        Output[] outputs = getOutputs();
        int outputStartIndex = inputs.length;
        for (int i = 0; i < outputs.length; i++) {
            int coeff = (int) Math.round(coefficients[outputStartIndex + i]);
            if (coeff != 1) {
                sb.append(coeff).append(" ");
            }
            sb.append(outputs[i].toString());
            if (i < outputs.length - 1) {
                sb.append(" + ");
            }
        }
        
        return sb.toString();
    }
    
}

