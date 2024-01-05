package data.weapons;

import com.fs.starfarer.api.combat.*;

public class RepairEveryframe implements EveryFrameWeaponEffectPlugin {

  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused())return;
    weapon.setCurrHealth(weapon.getMaxHealth());

  }
}
