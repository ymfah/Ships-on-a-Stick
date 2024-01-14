package data.weapons;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;

public class TacticalStick implements EveryFrameWeaponEffectPlugin {

  protected ShipAPI stick = null;

  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused())return;
    weapon.setCurrHealth(weapon.getMaxHealth());

    ShipAPI ship = weapon.getShip();
    if(stick == null){
      stick = engine.getFleetManager(ship.getOwner()).spawnShipOrWing("ymfah_tacticalstick", weapon.getLocation(), weapon.getCurrAngle());

      //weapon.getAnimation().setAlphaMult(0f);

      //ToDO switch to missiles and set collision class as no FF - missiles have no bounds
      //stick.setCollisionClass(CollisionClass.PROJECTILE_NO_FF);
      //MissileAPI stick = (MissileAPI) engine.spawnProjectile(ship, null,"ymfah_tacticalstick",weapon.getLocation(),weapon.getCurrAngle(), null);
    }
    if(!ship.isAlive()) stick.setHitpoints(0);
    stick.setPhased(ship.isPhased());
    stick.getLocation().set(MathUtils.getPointOnCircumference(weapon.getLocation(), 500f, weapon.getCurrAngle()));
    stick.setFacing(weapon.getCurrAngle());
    stick.setAngularVelocity(ship.getAngularVelocity());
    stick.giveCommand(ShipCommand.ACCELERATE, null, 0);

  }
}
