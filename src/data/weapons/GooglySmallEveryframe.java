package data.weapons;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class GooglySmallEveryframe implements EveryFrameWeaponEffectPlugin {


  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused())return;
    ShipAPI ship = weapon.getShip();
    if (!ship.isAlive())return;
    if(ship.getParentStation()!=null)ship=ship.getParentStation(); //apply to host of modular ship

    if (Global.getSettings().getCurrentState() == GameState.COMBAT) {

      //graphics
      //int frame = (int) (Math.random() * 3);
      float targetAngle = VectorUtils.getAngle(weapon.getLocation(), ship.getMouseTarget());
      float delta = MathUtils.getShortestRotation(weapon.getCurrAngle(), targetAngle);

      delta *= amount;

      float maxRotationSpeed = weapon.getTurnRate() * amount;
      MathUtils.clamp(delta, -maxRotationSpeed, maxRotationSpeed);

      weapon.setCurrAngle(weapon.getCurrAngle() + delta);

    } 
  }





}
