## 2026-09-22 - CSV Formula Injection Mitigation in Data Exports
**Vulnerability:** User-controlled values (student name, student ID, test name) exported to CSV files were vulnerable to CSV Formula Injection if they started with formula trigger characters (`=`, `+`, `-`, `@`, `\t`, `\r`).
**Learning:** `escapeCsv` was escaping commas and double quotes for CSV format compliance, but did not sanitize leading formula execution triggers that spreadsheet programs (Excel, Google Sheets) execute upon opening.
**Prevention:** Prepend a single quote (`'`) to any string starting with formula execution triggers (`=`, `+`, `-`, `@`, `\t`, `\r`) during CSV generation.
