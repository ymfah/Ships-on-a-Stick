package data.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class HoundOnAStickEveryframe implements EveryFrameWeaponEffectPlugin {

  protected ShipAPI stick = null;

  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused())return;
    ShipAPI ship = weapon.getShip();
    if (ship.getOriginalOwner() == -1)return;
    weapon.setCurrHealth(weapon.getMaxHealth());

    if(stick == null){
      engine.getFleetManager(ship.getOwner()).setSuppressDeploymentMessages(true);
      stick = engine.getFleetManager(ship.getOwner()).spawnShipOrWing("hound_luddic_path_Attack", weapon.getLocation(), weapon.getCurrAngle()); //works with hound_luddic_path_Attack, crashes when custom with hullmods?
      stick.setCollisionClass(CollisionClass.FIGHTER);
      engine.getFleetManager(ship.getOwner()).setSuppressDeploymentMessages(false);
      weapon.getAnimation().setAlphaMult(0f);
    }
    stick.setPhased(ship.isPhased());
    /*
    for (ShipHullSpecAPI randoship : Global.getSettings().getAllShipHullSpecs()){
      randoship.getBaseHullId(); //random ship picker I guess
    }
    */

    stick.getLocation().set(MathUtils.getPointOnCircumference(weapon.getLocation(), 5f, weapon.getCurrAngle())); //ship lower bound - weapon lower bound
    stick.setFacing(weapon.getCurrAngle());


  }
}
