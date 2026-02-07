package backend.constants;

public class Molecule implements ChemicalEntity {
    private String formula;
    
    public Molecule(String formula) {
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
