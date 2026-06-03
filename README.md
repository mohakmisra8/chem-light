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

## Publishing to PyPI

### One-time setup

1. Create accounts on [PyPI](https://pypi.org/account/register/) and [TestPyPI](https://test.pypi.org/account/register/) (optional, for dry runs).
2. On PyPI: **Your projects → Add new project** (name `chemlight` must be available).
3. **Trusted publishing** (recommended, no long-lived API token):
   - PyPI → **Account settings → Publishing** → **Add a new pending publisher**
   - Owner: `mohakmisra8`, repository: `equation-balancer`, workflow: `Publish to PyPI`, environment: `pypi`
   - In GitHub: **Settings → Environments** → create environment `pypi` (no secrets required for OIDC).
4. First manual upload (optional): `pip install twine`, `twine upload dist/*` with a PyPI API token.

### Automated release (GitHub Actions)

- **CI** (`.github/workflows/ci.yml`): on every push/PR to `main`, runs `pytest` and `python -m build` on Python 3.9–3.13.
- **Publish** (`.github/workflows/publish.yml`): when you push a tag `v*`, runs tests + build, then uploads `dist/` to PyPI if they pass.

Release steps:

```bash
# 1. Bump version in pyproject.toml (e.g. 0.1.0 → 0.1.1), commit
git add pyproject.toml && git commit -m "[chem-x] Release 0.1.1"

# 2. Tag must match the version (v + version from pyproject.toml)
git tag v0.1.1
git push origin main
git push origin v0.1.1   # triggers publish workflow
```

Test on TestPyPI first by adding a second trusted publisher and a `testpypi` environment, or run `twine upload --repository testpypi dist/*`.

## License

MIT
