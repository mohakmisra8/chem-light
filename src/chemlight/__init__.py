"""
chemLight — a lightweight chemistry library.

Balance chemical equations, look up elements, and validate electron shell rules.
"""

from chemlight.elements import Element
from chemlight.entities import ChemicalEntity, Compound, Molecule
from chemlight.equations import (
    BalancedEquation,
    Equation,
    Input,
    Output,
    Term,
    equation_from_terms,
    parse_equation,
)
from chemlight.exceptions import ShellConfigurationError
from chemlight.solver import balance
from chemlight.valence import (
    ValenceShell,
    max_electrons_for_shell,
    shells_in_energy_order,
    two_n_squared,
    validate_electron_configuration,
)

__version__ = "0.1.0"

__all__ = [
    "Element",
    "ChemicalEntity",
    "Molecule",
    "Compound",
    "Equation",
    "BalancedEquation",
    "Term",
    "Input",
    "Output",
    "parse_equation",
    "equation_from_terms",
    "balance",
    "ShellConfigurationError",
    "ValenceShell",
    "two_n_squared",
    "validate_electron_configuration",
    "max_electrons_for_shell",
    "shells_in_energy_order",
    "__version__",
]
