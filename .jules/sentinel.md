## 2026-09-26 - CSV Formula Injection in Exported Reports
**Vulnerability:** User input values (e.g., student name, ID) starting with formula characters (`=`, `+`, `-`, `@`, `\t`, `\r`) are executed as dynamic formulas when CSV reports are opened in Excel/Google Sheets.
**Learning:** CSV exports must sanitize all formula trigger characters before standard CSV quoting to prevent spreadsheet command execution / data exfiltration.
**Prevention:** Prefix all string fields starting with `=`, `+`, `-`, `@`, `\t`, or `\r` with a single quote `'` in `escapeCsv`.
