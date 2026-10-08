# Application package dependencies

The Android build contains one module, `:app`. Its sources contain the composition package `app`, nine context packages and `shared`.

- `app` composes context factories, navigation and cross-context event routing.
- Each context uses its own domain, application, infrastructure and presentation layers.
- Context packages use shared design components and technical contracts.
- Domain code does not import Android UI, Retrofit or persistence implementations.
- Feature packages do not import another context implementation.

These rules are checked by `ArchitectureBoundariesTest` in the app unit suite.
