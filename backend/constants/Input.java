package backend.constants;

public class Input {
    private ChemicalEntity content; // Can be Elements, Molecule, or Compound
    
    public Input(ChemicalEntity entity) {
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
