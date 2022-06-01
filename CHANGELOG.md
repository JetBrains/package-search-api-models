# Package Search API models Changelog

You can find more details on what changes between API revisions by checking out the swagger
[changelog file](./swagger/CHANGELOG.md).

## Version 2.3.0 (2 Dec 2021)

Matches v2.3.0 API swagger ([swagger-v2.3.0.yaml](./swagger/swagger-v2.3.0.yaml))

* Change POM license stats return type from object to array of POMLicenseStats object.

## Version 2.2.5 (17 Aug 2021)

Matches v2.2.5 API swagger ([swagger-v2.2.5.yaml](./swagger/swagger-v2.2.5.yaml))

* Added all the data classes under `com.jetbrains.packagesearch.api.statistics.*` for statistics

## Version 2.2.4 (1 Jun 2021)

* Added `ApiRepository.ApiNamedLinks`

## Version 2.2.3 (27 Apr 2021)

* Started deprecation cycle for `ApiStandardPackage.ApiPlatform.Companion` values in favor of enum
  class `ApiStandardPackage.ApiPlatform.PlatformType`
* Added enum for K/MP targets `ApiStandardPackage.ApiPlatform.PlatformTarget`

## Version 2.2.2 (18 Mar 2021)

Matches v2.2.2 API swagger ([swagger-v2.2.2.yaml](./swagger/swagger-v2.2.2.yaml))

* Added `user_facing_url`, `package_count`, `artifact_count` to the `/repositories` endpoint
* Made `url`, `type` and `friendly_name` mandatory in `/repository` to reflect actual db structure
* Removed `X-Request-Id` response headers to `/repositories` & `/package*` endpoints

## Version 2.2.0 (15 Sep 2020)

Matches v2.2.0 API swagger ([swagger-v2.2.0.yaml](./swagger/swagger-v2.2.0.yaml))

* Add `com.jetbrains.packagesearch.api.v1.ApiLegacyResponse`

## Version 2.1.0 (21 Jul 2020)

Matches v2.1.0 API swagger ([swagger-v2.1.0.yaml](./swagger/swagger-v2.1.0.yaml))

## Version 2.0.0 (28 Apr 2020)

Matches v2.0.0 API swagger ([swagger-v2.0.0.yaml](./swagger/swagger-v2.0.0.yaml))
