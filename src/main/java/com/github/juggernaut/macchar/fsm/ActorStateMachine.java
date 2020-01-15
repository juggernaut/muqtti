package com.github.juggernaut.macchar.fsm;

import com.github.juggernaut.macchar.Actor;

/**
 * @author ameya
 */
public abstract class ActorStateMachine implements StateMachine {

    private Actor actor;

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public Actor getActor() {
        return actor;
    }
}
