import re

import chemlight
import pytest
from chemlight import Molecule, equation_from_terms

_FORMULA_PATTERN = re.compile(r"([A-Z][a-z]*)(\d*)")


def _parse_formula(formula: str) -> dict[str, int]:
    counts: dict[str, int] = {}
    for element, count_str in _FORMULA_PATTERN.findall(formula):
        count = int(count_str) if count_str else 1
        counts[element] = counts.get(element, 0) + count
    return counts


def _element_totals(terms: list[str], coefficients: list[float]) -> dict[str, float]:
    totals: dict[str, float] = {}
    for formula, coeff in zip(terms, coefficients):
        for element, count in _parse_formula(formula).items():
            totals[element] = totals.get(element, 0.0) + coeff * count
    return totals


def assert_stoichiometry_conserved(result: chemlight.BalancedEquation) -> None:
    """Every element must appear in equal amounts on both sides."""
    reactant_formulas = [str(t) for t in result.inputs]
    product_formulas = [str(t) for t in result.outputs]
    n = len(reactant_formulas)
    lhs = _element_totals(reactant_formulas, result.coefficients[:n])
    rhs = _element_totals(product_formulas, result.coefficients[n:])
    assert lhs.keys() == rhs.keys()
    for element in lhs:
        assert lhs[element] == pytest.approx(rhs[element], rel=1e-9, abs=1e-6)


def assert_positive_integer_coefficients(result: chemlight.BalancedEquation) -> None:
    for coeff in result.coefficients:
        assert coeff > 0
        assert coeff == pytest.approx(round(coeff), abs=1e-9)


def test_balance_water_formation():
    result = chemlight.balance("H2 + O2 -> H2O")
    assert str(result) == "2 H2 + O2 -> 2 H2O"


def test_balance_from_equation_object():
    eq = equation_from_terms(
        [Molecule("H2"), Molecule("O2")],
        [Molecule("H2O")],
    )
    result = chemlight.balance(eq)
    assert result.coefficients == [2.0, 1.0, 2.0]


@pytest.mark.parametrize(
    "equation,expected",
    [
        ("CH4 + O2 -> CO2 + H2O", "CH4 + 2 O2 -> CO2 + 2 H2O"),
        ("C3H8 + O2 -> CO2 + H2O", "C3H8 + 5 O2 -> 3 CO2 + 4 H2O"),
        ("C2H5OH + O2 -> CO2 + H2O", "C2H5OH + 3 O2 -> 2 CO2 + 3 H2O"),
        ("C2H2 + O2 -> CO2 + H2O", "2 C2H2 + 5 O2 -> 4 CO2 + 2 H2O"),
        (
            "C6H12O6 + O2 -> CO2 + H2O",
            "C6H12O6 + 6 O2 -> 6 CO2 + 6 H2O",
        ),
        ("Fe + O2 -> Fe2O3", "4 Fe + 3 O2 -> 2 Fe2O3"),
        ("Al + O2 -> Al2O3", "4 Al + 3 O2 -> 2 Al2O3"),
        ("P4 + O2 -> P4O10", "P4 + 5 O2 -> P4O10"),
        ("N2 + H2 -> NH3", "N2 + 3 H2 -> 2 NH3"),
        ("SO2 + O2 -> SO3", "2 SO2 + O2 -> 2 SO3"),
        ("KClO3 -> KCl + O2", "2 KClO3 -> 2 KCl + 3 O2"),
        ("H2SO4 + NaOH -> Na2SO4 + H2O", "H2SO4 + 2 NaOH -> Na2SO4 + 2 H2O"),
        (
            "Na2CO3 + HCl -> NaCl + H2O + CO2",
            "Na2CO3 + 2 HCl -> 2 NaCl + H2O + CO2",
        ),
        (
            "CaCO3 + HCl -> CaCl2 + H2O + CO2",
            "CaCO3 + 2 HCl -> CaCl2 + H2O + CO2",
        ),
    ],
)
def test_balance_common_reactions(equation, expected):
    result = chemlight.balance(equation)
    assert str(result) == expected
    assert_stoichiometry_conserved(result)
    assert_positive_integer_coefficients(result)


def test_balance_ammonia_oxidation():
    result = chemlight.balance("NH3 + O2 -> NO + H2O")
    assert str(result) == "4 NH3 + 5 O2 -> 4 NO + 6 H2O"
    assert result.coefficients == [4.0, 5.0, 4.0, 6.0]


def test_balance_photosynthesis_reverse():
    """Six-species equation with a large nullspace."""
    result = chemlight.balance("CO2 + H2O -> C6H12O6 + O2")
    assert str(result) == "6 CO2 + 6 H2O -> C6H12O6 + 6 O2"
    assert_stoichiometry_conserved(result)


def test_balance_permanganate_redox_many_products():
    """Five products — tests search over a higher-dimensional nullspace."""
    result = chemlight.balance("KMnO4 + HCl -> KCl + MnCl2 + H2O + Cl2")
    assert str(result) == "2 KMnO4 + 16 HCl -> 2 KCl + 2 MnCl2 + 8 H2O + 5 Cl2"
    assert result.coefficients == pytest.approx(
        [2.0, 16.0, 2.0, 2.0, 8.0, 5.0], rel=1e-9
    )
    assert_stoichiometry_conserved(result)


def test_balance_from_equation_object_propane():
    eq = equation_from_terms(
        [Molecule("C3H8"), Molecule("O2")],
        [Molecule("CO2"), Molecule("H2O")],
    )
    result = chemlight.balance(eq)
    assert str(result) == "C3H8 + 5 O2 -> 3 CO2 + 4 H2O"
    assert result.coefficients == [1.0, 5.0, 3.0, 4.0]


def test_element_lookup():
    fe = chemlight.Element.get_by_symbol("Fe")
    assert fe is not None
    assert fe.atomic_number == 26
    assert str(fe) == "Fe"


def test_valence_shell_rule():
    assert chemlight.two_n_squared(2) == 8
    assert chemlight.max_electrons_for_shell(3) == 18
