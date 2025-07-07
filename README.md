
# senior-accounting-officer-registration-frontend

This is a placeholder README.md for a new repository

## How to use the service

Start dependent services using service-manager

`sm2 --start SAO_ALL`

To run the service locally, stop the service manager instance using

`sm2 --stop SENIOR_ACCOUNTING_OFFICER_REGISTRATION_FRONTEND`

Run the frontend locally using

`sbt 'run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`

Goto http://localhost:10057/senior-accounting-officer/registration


## Testing

### Feature toggles
All feature toggles are defined in `application.conf` under `features`.
The internal model for feature toggles are kept at `models.config.FeatureToggle`.

If test-only routes are enabled, then feature toggles can be configured for a given instance at runtime via

http://localhost:10057/senior-accounting-officer/registration/test-only/feature-toggle

### Testing GRS integration locally
Start dependent GRS services using service-manager

`sm2 --start INCORPORATED_ENTITY_IDENTIFICATION INCORPORATED_ENTITY_IDENTIFICATION_FRONTEND`

Set the `features.stubGrs` feature to false e.g. `features.stubGrs=false` or disable `Stub GRS` on the feature toggle page

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").