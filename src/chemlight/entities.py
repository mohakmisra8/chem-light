"""Chemical entity types: molecules and compounds."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Protocol, runtime_checkable


@runtime_checkable
class ChemicalEntity(Protocol):
    """Anything that can appear in an equation term (element, molecule, or compound)."""

    def __str__(self) -> str: ...


@dataclass(frozen=True)
class Molecule:
    """A molecular formula (e.g. H2O, CO2)."""

    formula: str

    def __str__(self) -> str:
        return self.formula


@dataclass(frozen=True)
class Compound:
    """A compound formula; same role as Molecule for balancing purposes."""

    formula: str

    def __str__(self) -> str:
        return self.formula
