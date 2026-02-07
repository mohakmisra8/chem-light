package backend.constants;

public class Output {
    private ChemicalEntity content; // Can be Elements, Molecule, or Compound
    
    public Output(ChemicalEntity entity) {
        this.content = entity;
    }
    
    public ChemicalEntity getContent() {
        return content;
    }
    
    @Override
    public String toString() {
        return content.toString();
    }
}
