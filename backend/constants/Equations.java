package backend.constants;

public class Equations {
    protected Input[] inputs;
    protected Output[] outputs;

    public Equations(Input[] inputs, Output[] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }
    
    public Input[] getInputs() {
        return inputs;
    }
    
    public Output[] getOutputs() {
        return outputs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Build input string: "Input A + Input B + ..."
        for (int i = 0; i < inputs.length; i++) {
            sb.append(inputs[i].toString());
            if (i < inputs.length - 1) {
                sb.append(" + ");
            }
        }
        
        // Add arrow separator
        sb.append(" -> ");
        
        // Build output string: "Output A + Output B + ..."
        for (int i = 0; i < outputs.length; i++) {
            sb.append(outputs[i].toString());
            if (i < outputs.length - 1) {
                sb.append(" + ");
            }
        }
        
        return sb.toString();
    }
}
