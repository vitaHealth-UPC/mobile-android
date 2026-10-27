# Android adapters against the local Spring backend

This opt-in probe uses production Retrofit adapters over a real TCP connection
to Spring, JPA and a fresh in-memory H2 database. It does not use MockWebServer,
mock controllers or shared team accounts. It is separate from ordinary JVM tests.

## Run

1. In the backend `fix/backend-architecture` worktree, compile test sources and
   obtain the test runtime classpath with Maven `test-compile dependency:build-classpath`.
   Include `target/test-classes` and `target/classes` before that dependency classpath.
2. With JDK 26, launch
   `java -Dspring.devtools.restart.enabled=false -cp "<test-runtime-classpath>" com.vitahealth.tata.carelink.AndroidContractServer <private-fixture-path>`.
   The path must not already exist. Use a Java argument file if the Windows
   command exceeds its length limit. The helper binds only to 127.0.0.1 on a random
   port and creates its own verified caregiver, consented adult and recorded alert.
3. With JDK 21, in Android run
   `.\gradlew.bat :app:verifyLocalBackendContracts -Ptata.liveContext=<private-fixture-path> --console=plain`.
   The probe rejects non-local hosts. Do not print or publish the fixture file.
4. The probe signals completion in a sibling `.done` file. The server closes H2
   and deletes both files. A 15-minute timeout also closes the server.

## Assertions

- The actual Android command and contact adapter persist name, relationship and phone.
- A new authorized HTTP read returns those fields and unchanged adult name, birth date and creation evidence.
- The actual timeline reader accepts the two persisted schedule/open events.
- The actual consent adapter resolves the authenticated link and obtains a 204 withdrawal receipt.
- The old adult credential receives 401 from current-session.
- The caregiver credential receives 403 from the revoked clinical monitoring route.

## Evidence on 26-10-2026

`tata-live-backend-android-contracts-final-20261026.log` records
`LOCAL_BACKEND_CONTRACTS_PASS`. The Spring host log is
`tata-live-contract-server-final-20261026.log`, both in `C:/Users/david/.codex/tmp`.
The first launch failed because DevTools restarted the custom main with different
arguments; disabling restart resolved it. No production runtime configuration changed. The first Gradle run completed the
HTTP assertions but failed while storing its configuration cache; the task now
captures the context-path provider during configuration.

This proves these contracts against the current local backend. It does not prove
Render deployment, PostgreSQL behavior, the complete Compose-to-server path,
FCM delivery or all other bounded contexts. Device UI and session-composition
checks remain independent evidence.
