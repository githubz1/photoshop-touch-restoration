from __future__ import annotations

from pathlib import Path
from zipfile import BadZipFile, ZipFile


class APK:
    """Read files from an Android APK archive."""

    def __init__(self, filename: str | Path):
        self.path = Path(filename).expanduser().resolve()

        if not self.path.exists():
            raise FileNotFoundError(f"APK does not exist: {self.path}")

        if not self.path.is_file():
            raise IsADirectoryError(f"APK path is not a file: {self.path}")

        try:
            self._zip = ZipFile(self.path, "r")
        except BadZipFile as exc:
            raise ValueError(f"Not a valid APK/ZIP file: {self.path}") from exc

    def close(self) -> None:
        self._zip.close()

    def __enter__(self) -> APK:
        return self

    def __exit__(self, exc_type, exc_value, traceback) -> None:
        self.close()

    def names(self) -> list[str]:
        """Return all entries inside the APK."""
        return self._zip.namelist()

    def exists(self, name: str) -> bool:
        """Return whether an exact archive entry exists."""
        return name in self._zip.namelist()

    def read(self, name: str) -> bytes:
        """Read an archive entry as bytes."""
        return self._zip.read(name)

    def find(self, filename: str) -> str | None:
        """Find an archive entry by its basename."""
        target = filename.casefold()

        for entry in self._zip.namelist():
            if Path(entry).name.casefold() == target:
                return entry

        return None

    def extract_tt_pixel(self) -> bytes:
        """Locate and return the contents of TTPixel.swf."""
        entry = self.find("TTPixel.swf")

        if entry is None:
            raise FileNotFoundError("TTPixel.swf was not found in the APK.")

        return self.read(entry)
