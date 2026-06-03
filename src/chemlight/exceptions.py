"""Chemistry-specific exceptions."""


class ShellConfigurationError(Exception):
    """Raised when an electron shell configuration violates the 2n² rule."""
