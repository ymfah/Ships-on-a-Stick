package data.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;


public class LandingPlatformFrigateEveryframe implements EveryFrameWeaponEffectPlugin {

  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused()) return;

    ShipAPI ship = weapon.getShip();
    String systemHeldKey = ship.getId() + weapon.getSlot().getId() + "_ymfah_land_frigate";
    ShipAPI Platform_Held = (ShipAPI)Global.getCombatEngine().getCustomData().get(systemHeldKey);

    if(Platform_Held == null){
      if(!ship.isAlive() || weapon.isDisabled() || ship.isPhased() ||ship.getFluxTracker().isVenting()){
        return;
      }
      for(ShipAPI FrigateToLand : CombatUtils.getShipsWithinRange(weapon.getLocation(),1000f)){
        if(FrigateToLand.isFrigate() && FrigateToLand.getCollisionClass() == CollisionClass.SHIP && !FrigateToLand.isStationModule()
                && FrigateToLand.isAlive() && FrigateToLand != ship && FrigateToLand.getOriginalOwner() == ship.getOriginalOwner() && FrigateToLand.getOwner() == 0){ //TODO use ship current order
          Global.getCombatEngine().getCustomData().put(systemHeldKey,FrigateToLand);
          Platform_Held = (ShipAPI)Global.getCombatEngine().getCustomData().get(systemHeldKey);
          Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, weapon.getLocation(), ship.getVelocity());
          Platform_Held.getFluxTracker().showOverloadFloatyIfNeeded("Ship Landed!", Color.WHITE, 15f, true);
          Platform_Held.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(ship.getId(),ship.getMutableStats().getEnergyWeaponRangeBonus().getPercentMod());
          Platform_Held.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(ship.getId(),ship.getMutableStats().getBallisticWeaponRangeBonus().getPercentMod());
          break;
        }
      }
    }

    if(Platform_Held != null){
      if(!ship.isAlive() || weapon.isDisabled() || ship.isPhased() ||ship.getFluxTracker().isVenting()||!Platform_Held.isAlive()){
        detatchShip(Platform_Held,systemHeldKey);
        return;
      }

      setLocation(Platform_Held, weapon.getLocation());
      Platform_Held.setCollisionClass(CollisionClass.FIGHTER);

      normaliseFlux(ship,Platform_Held,amount);

      Platform_Held.blockCommandForOneFrame(ShipCommand.ACCELERATE);
      Platform_Held.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
      Platform_Held.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
      Platform_Held.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
      Platform_Held.setAngularVelocity(0f);
      Platform_Held.setFacing(weapon.getCurrAngle());
      Platform_Held.setCurrentCR(ship.getCurrentCR());

      if(weapon.isFiring()){
        for(WeaponAPI w : Platform_Held.getAllWeapons()){
          if(!w.isDecorative() && w.getType() != WeaponAPI.WeaponType.SYSTEM){
            w.setForceFireOneFrame(true);
          }

        }
      }
    }
  }
  public void setLocation(CombatEntityAPI entity, Vector2f location) {
    Vector2f dif = new Vector2f(location);
    Vector2f.sub(location, entity.getLocation(), dif);
    Vector2f.add(entity.getLocation(), dif, entity.getLocation());
    Vector2f.sub(entity.getVelocity(), entity.getVelocity(), entity.getVelocity());
  }
  public void normaliseFlux(ShipAPI host, ShipAPI stuck, float amount){
    if(!stuck.getFluxTracker().isOverloaded()){
      if(host.getFluxTracker().getCurrFlux() > stuck.getFluxTracker().getCurrFlux()){
        host.getFluxTracker().decreaseFlux(stuck.getMutableStats().getFluxDissipation().getModifiedValue()*amount);
        stuck.getFluxTracker().increaseFlux(stuck.getMutableStats().getFluxDissipation().getModifiedValue()*amount,true);
      } else {
        host.getFluxTracker().increaseFlux(stuck.getMutableStats().getFluxDissipation().getModifiedValue()*amount, false);
        stuck.getFluxTracker().decreaseFlux(stuck.getMutableStats().getFluxDissipation().getModifiedValue()*amount);
      }
    }
  }
  public void detatchShip(ShipAPI ship, String systemHeldKey){
    ship.setCollisionClass(CollisionClass.SHIP);
    ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(ship.getId());
    ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(ship.getId());
    Global.getCombatEngine().getCustomData().remove(systemHeldKey);
  }
}
