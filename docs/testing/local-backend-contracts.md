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

- Actual treatment adapters register a medication and draft, configure its regimen
  and reminders, activate it, then pause and resume it with acknowledged states.
- Actual inventory adapters initialize ten units and append a five-unit receipt.
- The adult agenda contains the activated treatment. Confirming one dose twice
  through the production confirmation adapter leaves fourteen units, proving
  idempotent consumption across Treatment, Intake and Inventory over HTTP.
- Accessibility changes persist after a fresh HTTP read without changing the
  notification policy. The adult session cannot change caregiver preferences.
- Production analytics adapters accept global and treatment-specific summaries
  and recommendations for the authorized adult, and reject an unauthorized scope.
  A successful empty summary remains empty; the probe does not invent metrics.
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

## Password recovery from Compose (27-10-2026)

Launch the same test host with `-Dtata.probe.recovery=true`. This opt-in mode
adds an isolated recovery account and its encrypted-mailbox code to the private
fixture. A test-only primary generator retains one random code for the lifetime
of that host, so resend timing cannot invalidate the fixture during APK setup.
Production code generation, password policy and code consumption are unchanged.

Install the debug and AndroidTest APKs, reverse the host's TCP port with adb,
and push the fixture to the app's external-files directory. Run
`PasswordRecoveryLiveBackendTest` with instrumentation argument
`tata.liveContext` pointing to that device file. Only loopback is accepted.
The test deletes its device fixture in `finally`; signal the host's sibling
`.done` file and remove the adb reverse after execution.

The native dialog requests recovery through the production anonymous Retrofit
adapter, submits the code and a new password, then waits for Spring's receipt.
HTTP assertions prove the prior session receives 401, the old password receives
401, the new password receives 200 and code reuse receives 400. The UI does not
close early and returns to access only on the explicit action.

`tata-recovery-compose-spring-final-20261027.log` records **OK (1 test)**,
23.606 seconds. The earlier attempt failed because a resend replaced the
fixture's code during APK preparation; it is not counted as a success.
This check uses real Spring/JPA/H2 over TCP and Compose/Retrofit, with controlled
test-only code generation. It does not send email or certify an external provider.
