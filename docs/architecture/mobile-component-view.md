# Mobile component view

`TataNavHost` renders routes using factories from `AppContainer`. Each route observes a ViewModel state and passes values and callbacks to Compose screens. Application handlers invoke repository contracts; infrastructure supplies Retrofit services, local storage and device adapters. The `shared` package provides the design system and technical primitives.
