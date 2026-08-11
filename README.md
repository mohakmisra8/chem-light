# chemLight

A lightweight Python chemistry library for balancing chemical equations, looking up elements, and validating electron shell configurations.

## Install (development)

```bash
pip install -e ".[dev]"
```

## Install (when published)

```bash
pip install chemlight
```

## Quick start

```python
import chemlight

# Balance from a string
balanced = chemlight.balance("H2 + O2 -> H2O")
print(balanced)  # 2 H2 + O2 -> 2 H2O

# Look up an element
oxygen = chemlight.Element.get_by_symbol("O")
print(oxygen.atomic_number)  # 8

# Build an equation explicitly
from chemlight import Molecule, equation_from_terms, balance

eq = equation_from_terms(
    [Molecule("Fe"), Molecule("O2")],
    [Molecule("Fe2O3")],
)
print(balance(eq))
```

## Package layout

```
src/chemlight/     # import as `chemlight`
tests/             # pytest suite
pyproject.toml     # build metadata (hatchling)
```

## Development

```bash
pytest
python -m build   # produces dist/ wheel and sdist for PyPI
```

## License

MIT
