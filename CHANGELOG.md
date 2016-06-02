# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]
### Changed
- JOhm.setPool returns JedisPool instead of void - so it can be used as factory-method for Spring Dependency Injection
- BREAKING CHANGE - JOhm.getAll is no longer supported by default - you have to add an explicit annotation to the model: [@SupportAll](src/main/java/redis/clients/johm/SupportAll.java)
- HA - Add Sentinel support - task #6 - now you can connect to Redis HA cluster via Redis sentinels and use JOhm with HA!
- fix NULL values for BigDecimal and BigInteger
- save - if not new - propagate deleteChildren flag and deleteIndexes=true
- added expireIndexes support - with an overloaded method "expire"
- jedis bump to 2.8.0 because of slow Apache Commons Pool (BaseGenericObjectPool - updateStatsReturn and updateStatsBorrow)


### Added
- Expire support: JOhm.expire(model, seconds)

## [0.6.5] - 2016-04-25
### Added
- embedded Redis for unit tests (com.github.kstyrc:embedded-redis:0.6)