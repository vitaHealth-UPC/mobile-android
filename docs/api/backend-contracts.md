# Backend contracts

Android consumes the `vitaHealth-UPC/web-services` API but does not mirror backend internals.

Rules:

- Retrofit request/response DTOs stay in each context's `infrastructure/remote` package.
- DTOs are mapped before they cross into Domain or Presentation.
- API paths and payloads must be documented here when the corresponding backend contract is finalized.
