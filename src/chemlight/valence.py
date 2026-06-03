"""Electron shell configuration helpers."""

from __future__ import annotations

from enum import Enum

from chemlight.exceptions import ShellConfigurationError


def two_n_squared(n: int) -> int:
    """Maximum electrons in shell *n* via the 2n² rule."""
    return 2 * n * n


class ValenceShell(Enum):
    """Principal quantum shells and their maximum electron capacities (2n²)."""

    K_SHELL = (1, 2)
    L_SHELL = (2, 8)
    M_SHELL = (3, 18)
    N_SHELL = (4, 32)
    O_SHELL = (5, 50)
    P_SHELL = (6, 72)
    Q_SHELL = (7, 98)

    def __init__(self, principal_quantum_number: int, max_electrons: int) -> None:
        expected = two_n_squared(principal_quantum_number)
        if max_electrons != expected:
            raise ShellConfigurationError(
                f"Shell {principal_quantum_number} must follow 2n² rule: "
                f"expected {expected} electrons, got {max_electrons}"
            )
        self.principal_quantum_number = principal_quantum_number
        self.max_electrons = max_electrons

    @property
    def max_valence_electrons(self) -> int:
        return 2 if self.principal_quantum_number == 1 else 8


def validate_electron_configuration(shell_number: int, electron_count: int) -> bool:
    """
    Validate that *electron_count* does not exceed the 2n² capacity for *shell_number*.

    Returns True when valid; raises :class:`ShellConfigurationError` otherwise.
    """
    max_electrons = two_n_squared(shell_number)
    if electron_count > max_electrons:
        raise ShellConfigurationError(
            f"Shell {shell_number} cannot hold more than {max_electrons} electrons "
            f"(2n² rule). Got {electron_count} electrons."
        )
    return True


def max_electrons_for_shell(n: int) -> int:
    """Maximum electrons for principal quantum number *n* (1–7)."""
    if n < 1 or n > 7:
        raise ShellConfigurationError("Shell number must be between 1 and 7")
    return two_n_squared(n)


def shells_in_energy_order() -> list[ValenceShell]:
    """Shells ordered by increasing principal quantum number (lowest energy first)."""
    return list(ValenceShell)
