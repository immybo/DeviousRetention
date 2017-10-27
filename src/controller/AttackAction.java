package controller;

import model.Entity;
import model.World;
import model.entity.OwnedEntity;
import model.entity.Unit;

/**
 * Created by Robert Campbell on 27/10/2017.
 */
public class AttackAction implements Action {
    private final int attackerId;
    private final int defenderId;

    public AttackAction(int attackerId, int defenderId) {
        this.attackerId = attackerId;
        this.defenderId = defenderId;
    }

    @Override
    public void run(World world) {
        Entity attacker;
        Entity defender;
        try {
            attacker = world.getEntityByID(attackerId);
            defender = world.getEntityByID(defenderId);
        } catch (IllegalArgumentException e) {
            // One of the entities wasn't found; probably because it has been killed in the meantime
            return;
        }

        if (attacker instanceof Unit && defender instanceof OwnedEntity) {
            Unit attackerUnit = (Unit)attacker;
            if (!attackerUnit.canAttack(defender)) {
                throw new IllegalArgumentException("Attacker can't attack defender.");
            }

            attackerUnit.setTarget((OwnedEntity)defender);
        } else {
            throw new IllegalArgumentException("Attacker not a unit or defender is unowned entity.");
        }
    }
}
