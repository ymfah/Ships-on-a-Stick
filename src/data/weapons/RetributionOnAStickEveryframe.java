package data.weapons;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

public class RetributionOnAStickEveryframe implements EveryFrameWeaponEffectPlugin {

  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused())return;
    weapon.setCurrHealth(weapon.getMaxHealth());

    ShipAPI ship = weapon.getShip();

    for(MissileAPI missile : CombatUtils.getMissilesWithinRange(ship.getLocation(), 300)){
      if(missile.getProjectileSpecId().equals("orion_device_bomb") && missile.isFizzling()){
        for(ShipAPI allships : CombatUtils.getShipsWithinRange(missile.getLocation(), 300)){
          float angle =  VectorUtils.getAngle(missile.getLocation(),allships.getLocation());
          float distance = MathUtils.getDistance(missile.getLocation(),allships.getLocation());
          CombatUtils.applyForce(allships,angle,(300-distance)*15);
        }
      }
    }

  }
}
