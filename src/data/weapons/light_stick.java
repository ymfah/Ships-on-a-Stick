package data.weapons;

import com.fs.starfarer.api.combat.*;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.util.ArrayList;
import java.util.List;

public class light_stick implements EveryFrameWeaponEffectPlugin {


	private final List<CombatEntityAPI> ReflectedProjectiles = new ArrayList<>();

	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused())return;
		ShipAPI ship = weapon.getShip();
		if (!ship.isAlive())return;
		if (weapon.isDisabled())return;
		if(ship.isPhased())return;
		if(ship.getFluxLevel()>0.8f)return;

		float stick_range = weapon.getRange();
		for (DamagingProjectileAPI Projectile : CombatUtils.getProjectilesWithinRange(MathUtils.getPointOnCircumference(weapon.getLocation(),stick_range/2,weapon.getCurrAngle()),stick_range)) {
			if (Projectile != null && Projectile.getLocation() != null && Projectile.getSource() != ship && Projectile.getWeapon() != null && Projectile.getCollisionClass() != CollisionClass.NONE && !this.ReflectedProjectiles.contains(Projectile)){
				weapon.setForceFireOneFrame(true);
				if(MathUtils.isPointWithinCircle(Projectile.getLocation(),MathUtils.getPointOnCircumference(weapon.getLocation(),stick_range/2,weapon.getCurrAngle()),stick_range/2)){
					if(weapon.isFiring()){
						DamagingProjectileAPI ReflectedProjectile = (DamagingProjectileAPI)engine.spawnProjectile(ship, Projectile.getWeapon(), Projectile.getWeapon().getId(), Projectile.getLocation(), Projectile.getFacing() + 180.0F, null);
						this.ReflectedProjectiles.add(ReflectedProjectile);
						this.ReflectedProjectiles.add(Projectile);

						Projectile.setCollisionClass(CollisionClass.NONE);
						Projectile.setFacing(Projectile.getFacing() + 180.0F);
						Projectile.setHitpoints(0f);

						ship.getFluxTracker().increaseFlux(Projectile.getDamageAmount(),false);
					}
				}
				float targetAngle = VectorUtils.getAngle(weapon.getLocation(),Projectile.getLocation());
				float delta = MathUtils.getShortestRotation(weapon.getCurrAngle(), targetAngle);
				delta *= amount;
				delta *= 2; //overshoot
				float maxRotationSpeed = weapon.getTurnRate() * amount;
				MathUtils.clamp(delta, -maxRotationSpeed, maxRotationSpeed);
				weapon.setCurrAngle(weapon.getCurrAngle() + delta);
			}

		}

	}
}
