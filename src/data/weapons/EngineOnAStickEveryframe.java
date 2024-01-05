package data.weapons;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

import java.awt.Color;

import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import org.lazywizard.lazylib.MathUtils;

public class EngineOnAStickEveryframe implements EveryFrameWeaponEffectPlugin {
  public static float SPEED_BONUS = 10f;
  public static float TURN_BONUS = 10f;


  public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    if (engine.isPaused())return;
    ShipAPI ship = weapon.getShip();
    if (!ship.isAlive())return;
    if(ship.getParentStation()!=null)ship=ship.getParentStation(); //apply to host of modular ship
    String uniqueID = ship.getId() + weapon.getSlot().getId(); //an ID to allow multiple thrusters on same ship + modular ships
    if (weapon.isDisabled()){
      ship.getMutableStats().getMaxSpeed().unmodify(uniqueID);
      ship.getMutableStats().getAcceleration().unmodify(uniqueID); // max at 0f, 0 at 90 or 270, -max at 180f
      ship.getMutableStats().getDeceleration().unmodify(uniqueID); // max at 180f, 0 at 90 or 270, -max at 0f
      ship.getMutableStats().getTurnAcceleration().unmodify(uniqueID);  // max at 90 or 270, 0 at 0 or 180
      ship.getMutableStats().getMaxTurnRate().unmodify(uniqueID); // max at 90 or 270, 0 at 0 or 180
      return;
    }

    if (Global.getSettings().getCurrentState() == GameState.COMBAT) {

      float targetAngle = getVectorThrusterDirection(weapon);
      float delta = MathUtils.getShortestRotation(weapon.getCurrAngle(), targetAngle);
      float thrustbonus = Math.max(0, 1 - (Math.abs(delta) / 90));

      delta *= amount;

      float maxRotationSpeed = weapon.getTurnRate() * amount;
      MathUtils.clamp(delta, -maxRotationSpeed, maxRotationSpeed);

      weapon.setCurrAngle(weapon.getCurrAngle() + delta);
      //get slot id to allow multiple thrusters
      ship.getMutableStats().getMaxSpeed().modifyPercent(uniqueID, SPEED_BONUS * thrustbonus); // max at 0f, 0 at 90 or 270, -max at 180f
      ship.getMutableStats().getAcceleration().modifyPercent(uniqueID, TURN_BONUS * thrustbonus); // max at 0f, 0 at 90 or 270, -max at 180f
      ship.getMutableStats().getDeceleration().modifyPercent(uniqueID, TURN_BONUS * thrustbonus); // max at 180f, 0 at 90 or 270, -max at 0f
      ship.getMutableStats().getTurnAcceleration().modifyPercent(uniqueID, TURN_BONUS * thrustbonus);  // max at 90 or 270, 0 at 0 or 180
      ship.getMutableStats().getMaxTurnRate().modifyPercent(uniqueID, TURN_BONUS * thrustbonus); // max at 90 or 270, 0 at 0 or 180

      //engine graphics

      Vector2f size = new Vector2f(10, 80);
      float length = 0;
      if(isThursting(weapon)) length = thrustbonus;

      int frame = (int) (Math.random() * 5) + 1;
      if (frame == weapon.getAnimation().getNumFrames()) frame = 1;
      weapon.getAnimation().setFrame(frame);

      SpriteAPI sprite = weapon.getSprite();

      float currlength = (sprite.getWidth() - size.x / 2)/(size.x / 2);

      length = currlength + (length - currlength)*3f*amount; //smooth out

      float width = length * size.x / 2 + size.x / 2;
      float height = length * size.y + (float) Math.random() * 3 + 3;
      sprite.setSize(width, height);
      sprite.setCenter(width / 2, height / 2);

      length = Math.max(0, Math.min(1, length));
      sprite.setColor(new Color(1f, 0.5f + length / 2, 0.75f + length / 4));


    } 
  }

  public static float getVectorThrusterDirection(WeaponAPI weapon){
    float result = 0;
    ShipAPI ship = weapon.getShip();
    ShipAPI host = ship;
    if(ship.getParentStation()!=null)host=ship.getParentStation();
    ShipEngineControllerAPI ec = host.getEngineController();
    boolean burn=false;
    boolean turn=false;

    float SA = weapon.getSlot().getAngle()+ship.getFacing();

    float SD = host.getFacing();
    float Dir_Move = SA;
    float Dir_Goal = SD;


    if(ec.isAccelerating()){Dir_Move=SD+180f; burn=true;}
    if(ec.isAcceleratingBackwards()){Dir_Move=SD+0.f; burn=true;}
    if(ec.isStrafingLeft()){Dir_Move=SD+(-90f); burn=true;}
    if(ec.isStrafingRight()){Dir_Move=SD+90f; burn=true;}

    if(ec.isAccelerating()&&ec.isStrafingLeft()){Dir_Move=SD+(-135f);}
    if(ec.isAccelerating()&&ec.isStrafingRight()){Dir_Move=SD+135f;}
    if(ec.isAcceleratingBackwards()&&ec.isStrafingLeft()){Dir_Move=SD+(-45f);}
    if(ec.isAcceleratingBackwards()&&ec.isStrafingRight()){Dir_Move=SD+45f;}

    if(ec.isDecelerating()){Dir_Move=VectorUtils.getFacing(host.getVelocity()); burn=true;}

    if(burn)Dir_Goal=Dir_Move;

    float SLA =  VectorUtils.getAngle(host.getLocation(),weapon.getLocation());
    float Dir_Turn=SLA;
    if(ec.isTurningLeft()){Dir_Turn=SLA-90f; turn=true;}
    if(ec.isTurningRight()){Dir_Turn=SLA+90f; turn=true;}

    if(turn)Dir_Goal=Dir_Turn;

    if(burn && turn){
      Dir_Goal=(Dir_Move+Dir_Turn)/2f;
    }



    return Dir_Goal;
  }

  public static boolean isThursting(WeaponAPI weapon){
    ShipAPI ship = weapon.getShip();
    ShipAPI host = ship;
    if(ship.getParentStation()!=null)host=ship.getParentStation();
    ShipEngineControllerAPI ec = host.getEngineController();

      return ec.isAcceleratingBackwards() || ec.isAccelerating() || ec.isDecelerating() || ec.isStrafingRight() || ec.isStrafingLeft() || ec.isTurningRight() || ec.isTurningLeft();


  }

}
