package com.github.juggernaut.muqtti.fsm;

import com.github.juggernaut.muqtti.Actor;

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
