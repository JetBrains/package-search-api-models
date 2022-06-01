# Package Search API changelog

## Version 2 APIs

## Version 2.3.0 (2 Dec 2021)

See v2.3.0 API swagger ([swagger-v2.3.0.yaml](swagger-v2.3.0.yaml))

* Change POM license stats return type from object to array of POMLicenseStats object.

## Version 2.2.5 (17 Aug 2021)

See v2.2.5 API swagger ([swagger-v2.2.5.yaml](swagger-v2.2.5.yaml))

* Added `/statistics` endpoint
* Added schemas for represent statistic responses

### Version 2.2.4 (1 Jun 2021)

See v2.2.4 API swagger ([swagger-v2.2.4.yaml](swagger-v2.2.4.yaml))

* Added `named_links` field to `/repository` items

### Version 2.2.2 (16 Mar 2021)

See v2.2.2 API swagger ([swagger-v2.2.2.yaml](swagger-v2.2.2.yaml))

* Added `user_facing_url`, `package_count`, `artifact_count` to the `/repositories` endpoint
* Cleaned up Swagger docs and fixed some inconsistencies
* Made `url`, `type` and `friendly_name` mandatory in `/repository` to reflect actual db structure

### Version 2.2.1 (15 Sep 2020)

See v2.2.1 API swagger ([swagger-v2.2.1.yaml](swagger-v2.2.1.yaml))

* Added `/analytics/request/selected-result` endpoint
* Added `X-Request-Id` & `X-Request-Executor-Token` response headers to `/repositories` & `/package*` endpoints
* Added definition of `ResultSelection` entity

### Version 2.2.0 (15 Sep 2020)

See v2.2.0 API swagger ([swagger-v2.2.0.yaml](swagger-v2.2.0.yaml))

* Add model for legacy response, `ApiLegacyResponseWrapper`, used when no accept header is provided in the request (
  backward compat mode for IntelliJ
  IDEA's built-in package name autocomplete)

### Version 2.1.1 (11 Sep 2020)

See v2.1.1 API swagger ([swagger-v2.1.1.yaml](swagger-v2.1.1.yaml))

* Tweak `V2Repository` schema to add `alternate_urls` and `friendly_name`.

### Version 2.1.0 (21 Jul 2020)

See v2.1.0 API swagger ([swagger-v2.1.0.yaml](swagger-v2.1.0.yaml))

_[No detailed change notes available]_

### Version 2.0.0 (28 Apr 2020)

See v2.0.0 API swagger ([swagger-v2.0.0.yaml](swagger-v2.0.0.yaml))

_[No detailed change notes available]_

## Version 1 APIs

This is the first formally specced API version. It's the first time we distinguish between minimal and standard response
types, based on the
provided `Accept` header. When no accept header is passed with a request, we assume it's a legacy API request and
return (as of API v2.1.1
implementation) the legacy response wrapper.

### Version 1.0.0 (29 Mar 2020)

See v1.0.0 API swagger ([swagger-v1.0.0.yaml](swagger-v1.0.0.yaml))

_[No detailed change notes available]_

## Legacy API

The legacy APIs were not formalised in any way and are only intended to be used internally. A notable user was the IJ
built-in suggestions for
packages in the editor when changing Gradle and Maven scripts. As of the time we implement v2.1.1, we're in the process
of turning off the legacy
endpoints (`/search`
and `/idea/*`) and will be returning 404s once the migration of old IJ clients to the v1 endpoints in legacy backward
compat mode is completed (no
accept header -> returning the legacy API format, which is a minimal v1 response wrapped in a JSON object). 
