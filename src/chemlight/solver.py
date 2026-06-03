"""Equation balancing via linear algebra (nullspace method)."""

from __future__ import annotations

import math
import re
from typing import Optional

from chemlight.entities import Molecule
from chemlight.equations import (
    BalancedEquation,
    Equation,
    Term,
    parse_equation,
)

_ELEMENT_PATTERN = re.compile(r"([A-Z][a-z]*)")
_FORMULA_PATTERN = re.compile(r"([A-Z][a-z]*)(\d*)")
_EPS = 1e-10
_SEARCH_RANGE = 20


def balance(equation: str | Equation) -> BalancedEquation:
    """
    Balance a chemical equation using the matrix nullspace method.

    Accepts an :class:`Equation` or a string such as ``H2 + O2 -> H2O``.

    Returns a :class:`BalancedEquation` with integer stoichiometric coefficients.
    """
    parsed = parse_equation(equation)
    equation_str = str(parsed)

    lhs, rhs = equation_str.split(" -> ", 1)
    lhs_terms = [t.strip() for t in lhs.split(" + ")]
    rhs_terms = [t.strip() for t in rhs.split(" + ")]

    element_list: list[str] = []
    seen: set[str] = set()
    for term in lhs_terms + rhs_terms:
        for element in _extract_elements(term):
            if element not in seen:
                seen.add(element)
                element_list.append(element)

    element_count = len(element_list)
    total_terms = len(lhs_terms) + len(rhs_terms)
    term_vectors: list[list[float]] = []

    for term in lhs_terms:
        counts = _parse_formula(term)
        term_vectors.append([counts.get(el, 0) for el in element_list])

    for term in rhs_terms:
        counts = _parse_formula(term)
        term_vectors.append([-counts.get(el, 0) for el in element_list])

    matrix_a = [
        [term_vectors[j][i] for j in range(total_terms)]
        for i in range(element_count)
    ]

    nullspace = _find_nullspace(matrix_a)
    if not nullspace:
        raise RuntimeError("Could not find nullspace. Equation may be invalid.")

    coefficients = _find_optimal_coefficients(nullspace, total_terms)

    balanced_inputs = [Term(Molecule(t)) for t in lhs_terms]
    balanced_outputs = [Term(Molecule(t)) for t in rhs_terms]

    return BalancedEquation(
        inputs=balanced_inputs,
        outputs=balanced_outputs,
        coefficients=coefficients,
    )


def _extract_elements(formula: str) -> set[str]:
    return set(_ELEMENT_PATTERN.findall(formula))


def _parse_formula(formula: str) -> dict[str, int]:
    counts: dict[str, int] = {}
    for element, count_str in _FORMULA_PATTERN.findall(formula):
        count = int(count_str) if count_str else 1
        counts[element] = counts.get(element, 0) + count
    return counts


def _find_nullspace(matrix: list[list[float]]) -> list[list[float]]:
    rows = len(matrix)
    cols = len(matrix[0])
    augmented = [row[:] for row in matrix]
    rank = _gaussian_elimination(augmented)

    pivot_cols: list[int] = []
    pivot_index = 0
    for i in range(rank):
        for j in range(pivot_index, cols):
            if abs(augmented[i][j]) > _EPS:
                pivot_cols.append(j)
                pivot_index = j + 1
                break

    is_free = [j not in pivot_cols for j in range(cols)]
    nullspace: list[list[float]] = []

    for free_var in range(cols):
        if not is_free[free_var]:
            continue
        null_vec = [0.0] * cols
        null_vec[free_var] = 1.0
        for i in range(rank - 1, -1, -1):
            pivot_col = pivot_cols[i]
            total = sum(augmented[i][j] * null_vec[j] for j in range(pivot_col + 1, cols))
            null_vec[pivot_col] = -total / augmented[i][pivot_col]
        nullspace.append(null_vec)

    return nullspace


def _gaussian_elimination(matrix: list[list[float]]) -> int:
    rows = len(matrix)
    cols = len(matrix[0])
    rank = 0

    for col in range(cols):
        if rank >= rows:
            break
        pivot_row = rank
        for i in range(rank + 1, rows):
            if abs(matrix[i][col]) > abs(matrix[pivot_row][col]):
                pivot_row = i

        if abs(matrix[pivot_row][col]) <= _EPS:
            continue

        if pivot_row != rank:
            matrix[rank], matrix[pivot_row] = matrix[pivot_row], matrix[rank]

        pivot_val = matrix[rank][col]
        for i in range(rank + 1, rows):
            factor = matrix[i][col] / pivot_val
            for j in range(col, cols):
                matrix[i][j] -= factor * matrix[rank][j]
        rank += 1

    return rank


def _find_optimal_coefficients(nullspace: list[list[float]], term_count: int) -> list[float]:
    if not nullspace:
        raise RuntimeError("Nullspace is empty")

    nullspace_dim = len(nullspace)
    try_nums = list(range(1, _SEARCH_RANGE + 1))
    combinations = _generate_combinations(try_nums, nullspace_dim, _SEARCH_RANGE)

    nullspace_matrix = [
        [nullspace[j][i] for j in range(nullspace_dim)]
        for i in range(term_count)
    ]

    best_norm = float("inf")
    best_vector: Optional[list[float]] = None

    for combo in combinations:
        vector = [0.0] * term_count
        for i in range(term_count):
            for j in range(nullspace_dim):
                vector[i] += nullspace_matrix[i][j] * combo[j]

        if not _is_valid_coefficient_vector(vector):
            continue

        if any(v < 0 for v in vector):
            vector = [-v for v in vector]

        norm = _l2_norm(vector)
        if norm < best_norm:
            best_norm = norm
            best_vector = vector[:]

    if best_vector is None:
        raise RuntimeError("Could not find valid integer coefficients")
    return best_vector


def _is_valid_coefficient_vector(vector: list[float]) -> bool:
    for val in vector:
        if abs(val - round(val)) > _EPS:
            return False
        if val <= 0 or abs(val) < _EPS:
            return False
    return True


def _generate_combinations(
    numbers: list[int], length: int, max_combinations: int
) -> list[list[int]]:
    result: list[list[int]] = []
    _combinations_with_repetition(numbers, length, [], result, max_combinations)
    return result


def _combinations_with_repetition(
    numbers: list[int],
    length: int,
    current: list[int],
    result: list[list[int]],
    max_count: int,
) -> None:
    if len(result) >= max_count:
        return
    if len(current) == length:
        result.append(current[:])
        return
    for num in numbers:
        current.append(num)
        _combinations_with_repetition(numbers, length, current, result, max_count)
        current.pop()
        if len(result) >= max_count:
            return


def _l2_norm(vector: list[float]) -> float:
    return math.sqrt(sum(v * v for v in vector))
