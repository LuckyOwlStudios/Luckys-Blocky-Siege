package net.luckystudios.entity.ai.crew;

/** Implemented on every {@code Mob} by a mixin, so crew state can live on the mob itself. */
public interface SiegeCrewHolder {
    SiegeCrewState blockysiege$getCrewState();
}
