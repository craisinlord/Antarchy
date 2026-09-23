# Changelog

## Unreleased

### Added

- Restored Elythia Cloud Sea biome selection through a lightweight custom biome source.
- Added explicit Architectury API dependency declarations and installation documentation.
- Added Russian localization credit for suheckii.

### Changed

- Elythia Cloud Sea selection now begins at the distinct high-altitude threshold of Y=250.
- Permanent portal destination searches now use dimension-type coordinate scaling and a wider active-portal search radius.
- Permanent portal frames now include their corner blocks.
- Fabric keybinding conflict handling now synchronizes both held state and press-edge state for sprint when it shares a key with Antarchy mount actions.
- Removed terrain-height variance requirements from the Tiger’s Eye shrine so it can generate in Cavaryn’s irregular underground terrain.

### Fixed

- Queen boss music now uses a server-synchronized combat state so it can start reliably on clients during the fight.
- NeoForge creative-tab population now ignores non-vanilla namespaces when augmenting vanilla tabs.

### Verification pending

- Fresh-world Cloud Sea and Stratoshark generation.
- Permanent portal linking in both directions and complete frame detection.
- Queen music start, stop, restart, and reconnect behavior.
- Fabric toggle sprint while riding Dorrie or Hercules Beetle with shared keybindings.
- Tiger’s Eye shrine generation and `/locate structure antarchy:tigers_eye_shrine` in a fresh Cavaryn world.
