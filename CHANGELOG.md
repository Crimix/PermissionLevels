# Permission Levels Changelog
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [1.16.5, 1.0.3] - 2022-08-15
- Fixed crash with Mixins on servers

## [1.16.5, 1.0.2] - 2022-08-14
- Added option to set `bypassPlayerLimit` when using the `op` or `xop` command.
- Added another command `bypassPlayerLimit` that directly sets if an op is allowed to bypass the player limit.

## [1.16.4, 1.0.1] - 2020-11-21
- Full stable release

## [1.16.4, 1.0.1-b1] - 2020-11-04
- Port to 1.16.4
- Will be in beta until some more Forge updates and mappings have been released.

## [1.16.3, 1.0.1] - 2020-10-04
- Using Mixins to disable Vanilla /op command, such that this mod makes a complete override of the /op command
- Changed such that a op cannot assign a higher op level to a player than the level of the op executing the command.
- This change also includes /op without a level argument, it will assign the default level from the config or the level of the op executing the command, dependent of which one is the lowest.
- Added level suggestions from 1 to the level of the player executing the op command.

## [1.16.3, 1.0.0] - 2020-10-04
- 1.16 release

## [1.15.2, 1.0.0] - 2020-10-04
- 1.15 release

# Example
## [MC-VERSION, VERSION] - Date of release
### Added
- 
### Changed
- 
### Deprecated
- 
### Removed
- 
### Fixed
- 
### Security
- 
