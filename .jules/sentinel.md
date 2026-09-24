## 2026-09-24 - CSV Formula Injection in Data Exports

**Vulnerability:** Student names and student IDs exported to CSV files in `DataBackupManager` were vulnerable to CSV Formula Injection if they contained leading characters like `=`, `+`, `-`, `@`, `\t`, or `\r`.

**Learning:** Unsanitized user input written directly to CSV files can execute arbitrary spreadsheet formulas or commands when opened by users in Microsoft Excel, LibreOffice, or Google Sheets.

**Prevention:** Always prepend a single quote (`'`) to any string field starting with formula characters (`=`, `+`, `-`, `@`, `\t`, `\r`) before wrapping or escaping CSV fields.
