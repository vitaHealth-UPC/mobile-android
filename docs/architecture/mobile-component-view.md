# Mobile component view

The single `:app` module contains nine bounded-context packages.
`TataNavHost` and `AppContainer` compose the contexts and their ViewModel
factories. Within a bounded context:

1. A Compose Screen renders state and reports user events. Its Route
   observes a ViewModel; neither owns API calls nor persistence.
2. The ViewModel builds an application Command or Query and invokes its
   Handler. It publishes UiState and coordinates lifecycle/cancellation.
3. A Handler validates input, applies domain rules and uses repository
   contracts or application ports. Queries may return read models rather
   than constructing aggregates.
4. Domain expresses pure business concepts and invariants without Android.
5. Infrastructure implements the contracts using Retrofit, Room,
   DataStore, WorkManager or other device adapters. Responses are mapped
   and validated before reaching application/presentation.

The course's three-column Android Architecture Flow is an overview;
the course's DDD/CQRS slides refine it into four logical layers.

Navigation callbacks and destinations internal to a context belong to
its presentation package. Cross-context routing belongs to the app host.
The Treatment navigation graph is still a pending extraction; do not
alter its visual screens during this architectural milestone.
