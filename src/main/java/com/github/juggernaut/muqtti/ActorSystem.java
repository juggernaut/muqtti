package com.github.juggernaut.muqtti;

import com.github.juggernaut.muqtti.fsm.ActorStateMachine;
import com.github.juggernaut.muqtti.fsm.StateMachine;
import com.github.juggernaut.muqtti.fsm.events.Event;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A bare-bones implementation of an actor system
 *
 * @author ameya
 */
public class ActorSystem {

    // Map associating an actor id to the actor object
    private final ConcurrentMap<String, Actor> actorMap = new ConcurrentHashMap<>();
    private final Thread[] executors;
    private final BlockingQueue<Runnable>[] taskQueues;
    private final int numProcessors;

    private static final Logger LOGGER = Logger.getLogger(ActorSystem.class.getName());

    public ActorSystem(int numProcessors) {
        this.numProcessors = numProcessors;
        taskQueues = new BlockingQueue[numProcessors];
        executors = new Thread[numProcessors];
        for (int i = 0; i < numProcessors; i++) {
            // TODO: bounded-capacity queues
            taskQueues[i] = new LinkedBlockingQueue<>();
            executors[i] = new Thread(new TaskDequeuer(taskQueues[i]));
        }
    }

    public void start() {
        for (Thread executor : executors) {
            executor.start();
        }
    }

    /**
     * Turns a state machine into a real actor
     *
     * @param stateMachine
     * @return
     */
    public Actor createActor(final ActorStateMachine stateMachine) {
        final var actor = new DefaultActor(stateMachine);
        final var previous = actorMap.putIfAbsent(actor.getId(), actor);
        if (previous != null) {
            throw new RuntimeException("Actor with id " + actor.getId() + " already exists");
        }
        stateMachine.setActor(actor);
        stateMachine.init();
        return actor;
    }

    private class DefaultActor implements Actor {

        private final String id;
        private final StateMachine stateMachine;

        public DefaultActor(String id, StateMachine stateMachine) {
            this.id = id;
            this.stateMachine = stateMachine;
        }

        public DefaultActor(StateMachine stateMachine) {
            this(UUID.randomUUID().toString(), stateMachine);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void sendMessage(Event event) {
            // TODO: find optimal hash function
            final int queueIdx = Math.abs(hashCode() % numProcessors);
            taskQueues[queueIdx].offer(() -> {
                try {
                    stateMachine.onEvent(event);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "State machine action threw exception", e);
                }
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DefaultActor that = (DefaultActor) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public void destroy() {
            try {
                stateMachine.shutDown();
            } finally {
                actorMap.remove(id);
            }
        }
    }

    private static class TaskDequeuer implements Runnable {

        private final BlockingQueue<Runnable> taskQueue;

        public TaskDequeuer(BlockingQueue<Runnable> taskQueue) {
            this.taskQueue = taskQueue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final Runnable task = taskQueue.poll();
                    if (task != null) {
                        task.run();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Exception during actor task execution", e);
                }
            }
        }
    }

}
