package backend.Tools;

import backend.constants.BalancedEquations;
import backend.constants.ChemicalEntity;
import backend.constants.Elements;
import backend.constants.Equations;
import backend.constants.Input;
import backend.constants.Molecule;
import backend.constants.Output;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class SolverEngine {
    
    /**
     * Balances a chemical equation using linear algebra (nullspace method)
     * @param equation The equation to balance
     * @return A balanced equation with coefficients
     */
    public static Equations balance(Equations equation) {
        // Convert equation to string format for parsing
        final var equationStr = equation.toString();
        
        // Parse the equation
        final var sides = equationStr.split(" -> ");
        if (sides.length != 2) {
            throw new IllegalArgumentException("Invalid equation format. Expected ' -> ' separator.");
        }
        
        final var lhs = sides[0];
        final var rhs = sides[1];
        
        // Split into terms
        final var lhsTerms = lhs.split(" \\+ ");
        final var rhsTerms = rhs.split(" \\+ ");
        
        // Find all unique elements
        final var uniqueElements = new LinkedHashSet<String>();
        for (final var term : lhsTerms) {
            uniqueElements.addAll(extractElements(term.trim()));
        }
        for (final var term : rhsTerms) {
            uniqueElements.addAll(extractElements(term.trim()));
        }
        
        final var elementList = new ArrayList<String>(uniqueElements);
        final var elementCount = elementList.size();
        final var totalTerms = lhsTerms.length + rhsTerms.length;
        
        // Create vectors for each term
        final var termVectors = new ArrayList<double[]>();
        
        // Process LHS terms
        for (final var term : lhsTerms) {
            final var vec = new double[elementCount];
            final var elementCounts = parseFormula(term.trim());
            for (var i = 0; i < elementList.size(); i++) {
                vec[i] = elementCounts.getOrDefault(elementList.get(i), 0);
            }
            termVectors.add(vec);
        }
        
        // Process RHS terms (make negative)
        for (final var term : rhsTerms) {
            final var vec = new double[elementCount];
            final var elementCounts = parseFormula(term.trim());
            for (var i = 0; i < elementList.size(); i++) {
                vec[i] = -elementCounts.getOrDefault(elementList.get(i), 0);
            }
            termVectors.add(vec);
        }
        
        // Build matrix A
        final var matrixA = new double[elementCount][totalTerms];
        for (var i = 0; i < totalTerms; i++) {
            for (var j = 0; j < elementCount; j++) {
                matrixA[j][i] = termVectors.get(i)[j];
            }
        }
        
        // Find nullspace
        final var nullspace = findNullspace(matrixA);
        
        if (nullspace.isEmpty()) {
            throw new RuntimeException("Could not find nullspace. Equation may be invalid.");
        }
        
        // Find optimal integer coefficients
        final var coefficients = findOptimalCoefficients(nullspace, totalTerms);
        
        // Build balanced equation
        final var balancedInputs = new Input[lhsTerms.length];
        final var balancedOutputs = new Output[rhsTerms.length];
        
        // Reconstruct inputs with coefficients
        for (var i = 0; i < lhsTerms.length; i++) {
            final var entity = parseChemicalEntity(lhsTerms[i].trim());
            balancedInputs[i] = new Input(entity);
        }
        
        // Reconstruct outputs with coefficients
        for (var i = 0; i < rhsTerms.length; i++) {
            final var entity = parseChemicalEntity(rhsTerms[i].trim());
            balancedOutputs[i] = new Output(entity);
        }
        
        // Create balanced equation with coefficients stored
        final var balanced = new BalancedEquations(balancedInputs, balancedOutputs, coefficients);
        return balanced;
    }
    
    /**
     * Extracts element symbols from a chemical formula
     */
    private static Set<String> extractElements(String formula) {
        final var elements = new HashSet<String>();
        final var pattern = Pattern.compile("([A-Z][a-z]*)");
        final var matcher = pattern.matcher(formula);
        while (matcher.find()) {
            elements.add(matcher.group(1));
        }
        return elements;
    }
    
    /**
     * Parses a chemical formula and returns element counts
     * Handles formulas like "H2O", "CaCO3", etc.
     */
    private static Map<String, Integer> parseFormula(String formula) {
        final var elementCounts = new HashMap<String, Integer>();
        
        // Pattern to match element symbol followed by optional number
        final var pattern = Pattern.compile("([A-Z][a-z]*)(\\d*)");
        final var matcher = pattern.matcher(formula);
        
        while (matcher.find()) {
            final var element = matcher.group(1);
            final var countStr = matcher.group(2);
            final var count = countStr.isEmpty() ? 1 : Integer.parseInt(countStr);
            elementCounts.put(element, elementCounts.getOrDefault(element, 0) + count);
        }
        
        return elementCounts;
    }
    
    /**
     * Parses a string into a ChemicalEntity (Element, Molecule, or Compound)
     * If it's a single element (like "H2", "O2"), tries to parse as Element
     * Otherwise, treats it as a Molecule/Compound
     */
    private static ChemicalEntity parseChemicalEntity(String str) {
        // Extract the element symbol (first part before any numbers)
        final var pattern = Pattern.compile("^([A-Z][a-z]*)");
        final var matcher = pattern.matcher(str);
        
        if (matcher.find()) {
            final var symbol = matcher.group(1);
            final var elementOpt = Elements.getBySymbol(symbol);
            
            // If it's a valid element symbol and the formula is just that element
            // (possibly with a subscript), try to return as Element
            // Otherwise, treat as Molecule/Compound
            return elementOpt
                .filter(element -> {
                    // Check if the entire string is just the element with optional number
                    final var fullPattern = Pattern.compile("^" + Pattern.quote(symbol) + "\\d*$");
                    return fullPattern.matcher(str).matches();
                })
                .map(element -> (ChemicalEntity) element)
                .orElse(new Molecule(str));
        }
        
        // Not a single element, treat as molecule/compound
        return new Molecule(str);
    }
    
    /**
     * Finds the nullspace of a matrix using Gaussian elimination
     */
    private static List<double[]> findNullspace(double[][] matrix) {
        final var rows = matrix.length;
        final var cols = matrix[0].length;
        
        // Create augmented matrix for row reduction
        final var augmented = new double[rows][cols];
        for (var i = 0; i < rows; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, cols);
        }
        
        // Gaussian elimination to row echelon form
        final var rank = gaussianElimination(augmented);
        
        // Find nullspace vectors
        final var nullspace = new ArrayList<double[]>();
        
        // For each free variable, create a nullspace vector
        final var isFree = new boolean[cols];
        final var pivotCols = new int[rank];
        var pivotIndex = 0;
        
        for (var i = 0; i < rank; i++) {
            for (var j = pivotIndex; j < cols; j++) {
                if (Math.abs(augmented[i][j]) > 1e-10) {
                    pivotCols[i] = j;
                    pivotIndex = j + 1;
                    break;
                }
            }
        }
        
        // Mark free variables
        for (var j = 0; j < cols; j++) {
            var isPivot = false;
            for (var i = 0; i < rank; i++) {
                if (pivotCols[i] == j) {
                    isPivot = true;
                    break;
                }
            }
            isFree[j] = !isPivot;
        }
        
        // Generate nullspace vectors
        for (var freeVar = 0; freeVar < cols; freeVar++) {
            if (isFree[freeVar]) {
                final var nullVec = new double[cols];
                nullVec[freeVar] = 1.0;
                
                // Back substitute
                for (var i = rank - 1; i >= 0; i--) {
                    var sum = 0.0;
                    for (var j = pivotCols[i] + 1; j < cols; j++) {
                        sum += augmented[i][j] * nullVec[j];
                    }
                    nullVec[pivotCols[i]] = -sum / augmented[i][pivotCols[i]];
                }
                nullspace.add(nullVec);
            }
        }
        
        return nullspace;
    }
    
    /**
     * Performs Gaussian elimination and returns the rank
     */
    private static int gaussianElimination(double[][] matrix) {
        final var rows = matrix.length;
        final var cols = matrix[0].length;
        var rank = 0;
        
        for (var col = 0; col < cols && rank < rows; col++) {
            // Find pivot
            var pivotRow = rank;
            for (var i = rank + 1; i < rows; i++) {
                if (Math.abs(matrix[i][col]) > Math.abs(matrix[pivotRow][col])) {
                    pivotRow = i;
                }
            }
            
            if (Math.abs(matrix[pivotRow][col]) > 1e-10) {
                // Swap rows
                if (pivotRow != rank) {
                    final var temp = matrix[pivotRow];
                    matrix[pivotRow] = matrix[rank];
                    matrix[rank] = temp;
                }
                
                // Eliminate
                for (var i = rank + 1; i < rows; i++) {
                    final var factor = matrix[i][col] / matrix[rank][col];
                    for (var j = col; j < cols; j++) {
                        matrix[i][j] -= factor * matrix[rank][j];
                    }
                }
                rank++;
            }
        }
        
        return rank;
    }
    
    /**
     * Finds optimal integer coefficients by brute force search
     */
    private static double[] findOptimalCoefficients(List<double[]> nullspace, int termCount) {
        if (nullspace.isEmpty()) {
            throw new RuntimeException("Nullspace is empty");
        }
        
        final var nullspaceDim = nullspace.size();
        final var searchRange = 20; // Search coefficients from 1 to 20
        
        final var tryNums = new ArrayList<Integer>();
        for (var i = 1; i <= searchRange; i++) {
            tryNums.add(i);
        }
        
        // Generate all combinations
        final var combinations = generateCombinations(tryNums, nullspaceDim, searchRange);
        
        var bestNorm = Double.MAX_VALUE;
        Optional<double[]> bestVectorOpt = Optional.empty();
        
        // Build nullspace matrix
        final var nullspaceMatrix = new double[termCount][nullspaceDim];
        for (var i = 0; i < termCount; i++) {
            for (var j = 0; j < nullspaceDim; j++) {
                nullspaceMatrix[i][j] = nullspace.get(j)[i];
            }
        }
        
        for (final var combo : combinations) {
            // Multiply nullspace matrix by combination coefficients
            final var vector = new double[termCount];
            for (var i = 0; i < termCount; i++) {
                for (var j = 0; j < nullspaceDim; j++) {
                    vector[i] += nullspaceMatrix[i][j] * combo.get(j);
                }
            }
            
            // Check if valid (all integers, all positive, no zeros)
            var isValid = true;
            for (final var val : vector) {
                if (Math.abs(val - Math.round(val)) > 1e-10) {
                    isValid = false;
                    break;
                }
                if (val <= 0 || Math.abs(val) < 1e-10) {
                    isValid = false;
                    break;
                }
            }
            
            if (isValid) {
                // Make all positive
                var allPositive = true;
                for (final var val : vector) {
                    if (val < 0) {
                        allPositive = false;
                        break;
                    }
                }
                
                if (!allPositive) {
                    // If negative, flip sign
                    for (var i = 0; i < vector.length; i++) {
                        vector[i] = -vector[i];
                    }
                }
                
                final var norm = calculateNorm(vector);
                if (norm < bestNorm) {
                    bestNorm = norm;
                    bestVectorOpt = Optional.of(vector.clone());
                }
            }
        }
        
        return bestVectorOpt.orElseThrow(() -> 
            new RuntimeException("Could not find valid integer coefficients"));
    }
    
    /**
     * Generates all combinations with repetition for brute force search
     * This generates all possible ways to assign coefficients to nullspace vectors
     */
    private static List<List<Integer>> generateCombinations(List<Integer> numbers, int length, int maxCombinations) {
        final var combinations = new ArrayList<List<Integer>>();
        generateCombinationsWithRepetition(numbers, length, new ArrayList<>(), combinations, maxCombinations);
        return combinations;
    }
    
    private static void generateCombinationsWithRepetition(List<Integer> numbers, int length, 
                                                           List<Integer> current, 
                                                           List<List<Integer>> result, int max) {
        if (result.size() >= max) return;
        
        if (current.size() == length) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        // Allow repetition - each position can be any number from the list
        for (final var num : numbers) {
            current.add(num);
            generateCombinationsWithRepetition(numbers, length, current, result, max);
            current.remove(current.size() - 1);
            if (result.size() >= max) break;
        }
    }
    
    /**
     * Calculates L2 norm of a vector
     */
    private static double calculateNorm(double[] vector) {
        var sum = 0.0;
        for (final var val : vector) {
            sum += val * val;
        }
        return Math.sqrt(sum);
    }
}


