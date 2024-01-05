package data.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class ReaperOnAStickEveryframe implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

  
  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused())return;
    ShipAPI ship = weapon.getShip();
    if(ship.isPhased())return;
    if(!ship.isAlive())return;

    weapon.setCurrHealth(weapon.getMaxHealth());

    Vector2f detectPoint = MathUtils.getPointOnCircumference(weapon.getLocation(),175f,weapon.getCurrAngle());

    for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship,700f)) {
      if (enemy.getOwner() != ship.getOwner() && !enemy.isFighter() && !enemy.isDrone() && !enemy.isShuttlePod()) {
        if(CollisionUtils.isPointWithinBounds(detectPoint,enemy)){
          weapon.setForceFireOneFrame(true);
          break;
        }
        if(enemy.getShield() != null){
          if(Math.abs(MathUtils.getShortestRotation(VectorUtils.getAngle(enemy.getLocation(), detectPoint), enemy.getShield().getFacing())) < enemy.getShield().getActiveArc() / 2){
            if(MathUtils.getDistance(detectPoint, enemy.getLocation()) - enemy.getShield().getRadius() < 0f){
              weapon.setForceFireOneFrame(true);
              break;
            }
          }

        }

      }


    }
  }

  public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    MissileAPI missile = (MissileAPI) projectile;
    missile.explode();
    Global.getCombatEngine().removeEntity(missile);
  }
}
