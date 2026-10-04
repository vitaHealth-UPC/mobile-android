# Module dependency map

```text
                    :app
                     |
   -----------------------------------------
   |    |      |      |      |      |      |
identity carelink treatment intake omission monitoring
   |      |       |      |       |      |
 analytics inventory preferences
          \      |      /
              :shared
```

Allowed:

- `:app -> :shared`
- `:app -> every Bounded Context`
- `each Bounded Context -> :shared`

Not allowed:

- `:treatment -> :intake`
- `:intake -> :analytics`
- any other Bounded Context to Bounded Context compile dependency

Cross-context propagation is routed by the composition root using explicit events/contracts.
