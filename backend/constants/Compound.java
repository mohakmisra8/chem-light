package backend.constants;

public class Compound implements ChemicalEntity {
    private String formula;
    
    public Compound(String formula) {
        this.formula = formula;
    }
    
    public String getFormula() {
        return formula;
    }
    
    @Override
    public String toString() {
        return formula;
    }
}
