package data.weapons;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;

public class OmenOnAStickEveryframe implements EveryFrameWeaponEffectPlugin {
  private static final Color EMP_COLOR = new Color(100, 70, 255);
  
  private List<ShipAPI> SHIPS = new ArrayList<>();
  
  private List<MissileAPI> MISSILES = new ArrayList<>();
  
  private List<ShipAPI> FIGHTERS = new ArrayList<>();
  
  private static final float RANGE = 500.0F;

  private static final float NO_TARGET_RANGE = 300.0F;
  
  private static final float ARC_DAMAGE = 100.0F;

  private static final float ARC_EMP_DAMAGE = 500.0F;

  
  float counter = 0.0F;
  
  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused())return;
    if (weapon.isDisabled())return;
    ShipAPI ship = weapon.getShip();
    if(ship.isPhased())return;
    if(!ship.isAlive())return;
    float damage = ARC_DAMAGE;
    float systemRangeBonus = ship.getMutableStats().getSystemRangeBonus().flatBonus + 1f;

    if (Global.getSettings().getCurrentState() == GameState.COMBAT) {
      this.counter += amount;

      Vector2f point = MathUtils.getPointOnCircumference(weapon.getLocation(), 35f, weapon.getCurrAngle()); //offset to make it fire from antenna


      if (this.counter > 0.2F && !ship.getFluxTracker().isOverloaded()) {
        this.counter = 0.0F;
        this.MISSILES.clear();
        this.FIGHTERS.clear();
        this.SHIPS.clear();
        for (ShipAPI enemy : engine.getShips()) {
          if (Misc.getDistance(point, enemy.getLocation()) > RANGE*systemRangeBonus) continue;
          if (enemy != ship && enemy.isAlive() && !ship.isAlly() && enemy.getOriginalOwner() != ship.getOriginalOwner()) {
            if (enemy.isFighter()) {
              this.FIGHTERS.add(enemy);
              continue;
            } 
            this.SHIPS.add(enemy);
          } 
        }
        for (MissileAPI missile : engine.getMissiles()) {
          if (Misc.getDistance(point, missile.getLocation()) > RANGE*systemRangeBonus) continue;
          if (missile.getSource() != null && missile.getSource() != ship && !missile.getSource().isAlly() && missile.getSource().getOriginalOwner() != ship.getOriginalOwner()) this.MISSILES.add(missile);
        }

        CombatEntityAPI target;
        if(!this.MISSILES.isEmpty()){
          int i = (int) ((Math.random() * this.MISSILES.size()));
          target = this.MISSILES.get(i);
        }else if(!this.FIGHTERS.isEmpty()){
          int i = (int) ((Math.random() * this.FIGHTERS.size()));
          target = this.FIGHTERS.get(i);
        }else if(!this.SHIPS.isEmpty()){
          int i = (int) ((Math.random() * this.SHIPS.size()));
          target = this.SHIPS.get(i);
        } else {
          if(Math.random()<0.1f){
            target = ship;
            damage = 0;
          } else {
            target = new SimpleEntity(MathUtils.getRandomPointInCircle(point, NO_TARGET_RANGE));
            damage = 0;
          }

        }
        engine.spawnEmpArc(ship, point, ship, target, DamageType.ENERGY, damage, ARC_EMP_DAMAGE, 100000.0F, "system_emp_emitter_impact", 15.0F, EMP_COLOR, new Color(255, 255, 255));


      } 
    } 
  }
}
