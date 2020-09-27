# Permission Levels Changelog
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [1.16.3, 1.0.0] - 2020-09-
- 1.16 release

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

This mod overrides the vanilla Minecraft's /op command to be able to take a permission level argument.  
This argument is a value between 1-4 corresponding to the levels of [Mincraft gamepedia - Commands/op](https://minecraft.gamepedia.com/Commands/op#Levels).  
This could be used together with mods that adds commends with different permissions levels to allow level 1 ops to use some commands but not the full list of command including stopping the server.

This mod add two commands.
1. The /op command which overrides vanilla Minecraft's own /op command.
This command takes the player and then a value between 1 and 4 for the permission level.
A unconfigured server would default the permission level to 4.
2. The /xop command, this command is the same as the /op command above, this is only added if some other mod decides to override /op also.