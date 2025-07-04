
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

### Testing GRS integration locally
Start dependent GRS services using service-manager

`sm2 --start INCORPORATED_ENTITY_IDENTIFICATION INCORPORATED_ENTITY_IDENTIFICATION_FRONTEND`

Set `features.stubGrs=false` either in application.conf or via `-Dfeatures.stubGrs=false` and restart the service

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").