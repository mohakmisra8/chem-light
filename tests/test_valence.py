import pytest

import chemlight


def test_validate_electron_configuration_ok():
    assert chemlight.validate_electron_configuration(2, 8) is True


def test_validate_electron_configuration_exceeds_capacity():
    with pytest.raises(chemlight.ShellConfigurationError):
        chemlight.validate_electron_configuration(1, 3)


def test_shells_in_energy_order():
    shells = chemlight.shells_in_energy_order()
    assert [s.principal_quantum_number for s in shells] == [1, 2, 3, 4, 5, 6, 7]
