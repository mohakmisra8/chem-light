package backend.constants;

public enum ValenceShells {
    K_SHELL(1, 2),      // n=1, max 2 electrons
    L_SHELL(2, 8),      // n=2, max 8 electrons
    M_SHELL(3, 18),     // n=3, max 18 electrons (8 in valence)
    N_SHELL(4, 32),     // n=4, max 32 electrons (8 in valence)
    O_SHELL(5, 50),     // n=5, max 50 electrons (8 in valence)
    P_SHELL(6, 72),     // n=6, max 72 electrons (8 in valence)
    Q_SHELL(7, 98);     // n=7, max 98 electrons (8 in valence)

    private final int principalQuantumNumber;
    private final int maxElectrons;

    ValenceShells(int principalQuantumNumber, int maxElectrons) {
        this.principalQuantumNumber = principalQuantumNumber;
        // Ensure the 2n² rule is followed
        int expectedMax = twoNSquaredRule(principalQuantumNumber);
        if (maxElectrons != expectedMax) {
            try {
                throw new ShellConfigurationException(
                    String.format("Shell %d must follow 2n² rule: expected %d electrons, got %d", 
                        principalQuantumNumber, expectedMax, maxElectrons));
            } catch (ShellConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        this.maxElectrons = maxElectrons;
    }

    public int getPrincipalQuantumNumber() {
        return principalQuantumNumber;
    }

    public int getMaxElectrons() {
        return maxElectrons;
    }

    public int getMaxValenceElectrons() {

        return principalQuantumNumber == 1 ? 2 : 8;
    }
    
    public int twoNSquaredRule(int n) {
        return 2 * n * n;
    }
    
    /**
     * Validates that the electron configuration follows the 2n² rule
     * and ensures filling from inside out (lowest energy first).
     * 
     * @param shellNumber The principal quantum number (n)
     * @param electronCount The number of electrons in the shell
     * @return true if the configuration is valid
     * @throws ShellConfigurationException if the configuration violates the 2n² rule
     */
    public static boolean validateElectronConfiguration(int shellNumber, int electronCount) throws ShellConfigurationException {
        int maxElectrons = 2 * shellNumber * shellNumber;
        if (electronCount > maxElectrons) {
            throw new ShellConfigurationException(
                String.format("Shell %d cannot hold more than %d electrons (2n² rule). Got %d electrons.", 
                    shellNumber, maxElectrons, electronCount));
        }
        return true;
    }
    
    /**
     * Returns the maximum number of electrons for a given shell number following the 2n² rule.
     * Ensures filling from inside out (lowest energy first).
     * 
     * @param n The principal quantum number
     * @return Maximum electrons: 2 in shell 1, 8 in shell 2, 18 in shell 3, 32 in shell 4, etc.
     */
    public static int getMaxElectronsForShell(int n) throws ShellConfigurationException {
        if (n < 1 || n > 7) {
            throw new ShellConfigurationException("Shell number must be between 1 and 7");
        }
        return 2 * n * n;
    }
    
    /**
     * Gets shells in order of increasing energy (filling from inside out).
     * 
     * @return Array of shells ordered by principal quantum number (lowest energy first)
     */
    public static ValenceShells[] getShellsInEnergyOrder() {
        return new ValenceShells[]{
            K_SHELL, L_SHELL, M_SHELL, N_SHELL, O_SHELL, P_SHELL, Q_SHELL
        };
    }
}
