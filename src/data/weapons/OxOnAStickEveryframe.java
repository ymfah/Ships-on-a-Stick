package data.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class OxOnAStickEveryframe implements EveryFrameWeaponEffectPlugin{

  public static float OX_RANGE = 400f;
  public static float OX_HELD_FLUX_PER_SECOND = 100f;
  public static float OX_YEET_FLUX = 500f;
  public static float MEATSPIN = 30f; //slowly spins the captured ship

  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused())return;
    ShipAPI ship = weapon.getShip();

    String systemHeldKey = ship.getId() + weapon.getSlot().getId() + "_ymfah_ox_held";
    ShipAPI OX_Held = (ShipAPI)Global.getCombatEngine().getCustomData().get(systemHeldKey);

    if(ship.isPhased() || !ship.isAlive() || weapon.isDisabled()){
      Global.getCombatEngine().getCustomData().remove(systemHeldKey);
      return;
    }

    if(OX_Held == null){
      for(ShipAPI StuffToThrow : CombatUtils.getShipsWithinRange(MathUtils.getPointOnCircumference(weapon.getLocation(),OX_RANGE-OX_RANGE/4,weapon.getCurrAngle()),OX_RANGE/4)){
          if(StuffToThrow.getCollisionClass() == CollisionClass.SHIP || StuffToThrow.getCollisionClass() == CollisionClass.ASTEROID){
            if(StuffToThrow.getHitpoints()!=0 && StuffToThrow.getOwner() != ship.getOwner() && !Global.getCombatEngine().getCustomData().containsValue(StuffToThrow)){
              Global.getCombatEngine().getCustomData().put(systemHeldKey,StuffToThrow);
              OX_Held = (ShipAPI)Global.getCombatEngine().getCustomData().get(systemHeldKey);
              Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, MathUtils.getPointOnCircumference(weapon.getLocation(),OX_RANGE,weapon.getCurrAngle()), OX_Held.getVelocity());
              OX_Held.getFluxTracker().showOverloadFloatyIfNeeded("Ship Yoinked!", Color.RED, 15f, true);
              break;
            }
          }
      }
    }
    if(OX_Held != null){
      if(ship.getFluxTracker().isVenting() || weapon.isFiring() || OX_Held.getHitpoints() == 0f){
        yeet(weapon, OX_Held, systemHeldKey);
      } else {
        setLocation(OX_Held, MathUtils.getPointOnCircumference(weapon.getLocation(),ship.getCollisionRadius()+OX_Held.getCollisionRadius(),weapon.getCurrAngle()));
        weapon.setCurrAngle(weapon.getCurrAngle()); //disable turn
        OX_Held.setFacing(OX_Held.getFacing()+MEATSPIN*amount);
        OX_Held.setJitterUnder(ship, getEffectColor(OX_Held), 1f, 10, 0f, 15f);
        ship.getFluxTracker().increaseFlux(OX_HELD_FLUX_PER_SECOND*amount*OX_Held.getMass()/ship.getMass(),false);
      }
    }
  }

  public void yeet(WeaponAPI weapon, ShipAPI OX_Held, String systemHeldKey){
    ShipAPI ship = weapon.getShip();
    weapon.disable();
    for (ShipEngineControllerAPI.ShipEngineAPI engine : OX_Held.getEngineController().getShipEngines()){
      engine.disable();
    }
    CombatUtils.applyForce(OX_Held,weapon.getCurrAngle(),10000f);
    Global.getSoundPlayer().playSound("rifttorpedo_explosion", 1f, 1f, MathUtils.getPointOnCircumference(weapon.getLocation(),OX_RANGE,weapon.getCurrAngle()), OX_Held.getVelocity());
    ship.getFluxTracker().increaseFlux(OX_YEET_FLUX*OX_Held.getMass()/ship.getMass(),true);
    Global.getCombatEngine().getCustomData().remove(systemHeldKey);
  }

  protected Color getEffectColor(ShipAPI ship) {
    if (ship.getEngineController().getShipEngines().isEmpty()) {
      return Color.DARK_GRAY;
    }
    return ship.getEngineController().getShipEngines().get(0).getEngineColor();
  }

  public void setLocation(CombatEntityAPI entity, Vector2f location) {
    Vector2f dif = new Vector2f(location);
    Vector2f.sub(location, entity.getLocation(), dif);
    Vector2f.add(entity.getLocation(), dif, entity.getLocation());
    Vector2f.sub(entity.getVelocity(), entity.getVelocity(), entity.getVelocity());
  }
}
