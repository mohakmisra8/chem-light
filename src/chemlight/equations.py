"""Chemical equations and reactant/product terms."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Sequence

from chemlight.entities import ChemicalEntity, Molecule


@dataclass
class Term:
    """One side of an equation: a single reactant or product."""

    entity: ChemicalEntity

    def __str__(self) -> str:
        return str(self.entity)


# Aliases matching the original API naming
Input = Term
Output = Term


@dataclass
class Equation:
    """An unbalanced chemical equation."""

    inputs: list[Term]
    outputs: list[Term]

    def __str__(self) -> str:
        lhs = " + ".join(str(t) for t in self.inputs)
        rhs = " + ".join(str(t) for t in self.outputs)
        return f"{lhs} -> {rhs}"


@dataclass
class BalancedEquation(Equation):
    """A balanced equation with stoichiometric coefficients."""

    coefficients: list[float]

    def __str__(self) -> str:
        parts: list[str] = []
        for i, term in enumerate(self.inputs):
            coeff = round(self.coefficients[i])
            prefix = f"{coeff} " if coeff != 1 else ""
            parts.append(f"{prefix}{term}")
        lhs = " + ".join(parts)

        parts = []
        offset = len(self.inputs)
        for i, term in enumerate(self.outputs):
            coeff = round(self.coefficients[offset + i])
            prefix = f"{coeff} " if coeff != 1 else ""
            parts.append(f"{prefix}{term}")
        rhs = " + ".join(parts)
        return f"{lhs} -> {rhs}"


def parse_equation(equation: str | Equation) -> Equation:
    """
    Parse a string like ``H2 + O2 -> H2O`` into an :class:`Equation`.

    Raises :class:`ValueError` if the format is invalid.
    """
    if isinstance(equation, Equation):
        return equation

    text = equation.strip()
    if " -> " not in text:
        raise ValueError("Invalid equation format. Expected ' -> ' separator.")

    lhs, rhs = text.split(" -> ", 1)
    inputs = [Term(Molecule(t.strip())) for t in lhs.split(" + ") if t.strip()]
    outputs = [Term(Molecule(t.strip())) for t in rhs.split(" + ") if t.strip()]

    if not inputs or not outputs:
        raise ValueError("Equation must have at least one reactant and one product.")

    return Equation(inputs=inputs, outputs=outputs)


def equation_from_terms(
    reactants: Sequence[ChemicalEntity],
    products: Sequence[ChemicalEntity],
) -> Equation:
    """Build an equation from explicit reactant and product entities."""
    return Equation(
        inputs=[Term(e) for e in reactants],
        outputs=[Term(e) for e in products],
    )
