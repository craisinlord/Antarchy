package com.craisinlord.antarchy.content.entity.royal.attack;

import java.util.HashMap;
import java.util.Map;

public final class RoyalEffectController {
    private record Effect(Runnable tick, Runnable end, int duration, int age) {
        private Effect advance() {
            return new Effect(this.tick, this.end, this.duration, this.age + 1);
        }
    }

    private final Map<String, Effect> active = new HashMap<>();

    public void tick() {
        for (var iterator = this.active.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            Effect effect = entry.getValue();
            effect.tick().run();
            Effect next = effect.advance();
            if (next.age() >= next.duration()) {
                next.end().run();
                iterator.remove();
            } else {
                entry.setValue(next);
            }
        }
    }

    public void start(String id, int duration, Runnable onStart, Runnable tick, Runnable end) {
        this.stop(id);
        onStart.run();
        this.active.put(id, new Effect(tick, end, Math.max(1, duration), 0));
    }

    public boolean active(String id) {
        return this.active.containsKey(id);
    }

    public void stop(String id) {
        Effect effect = this.active.remove(id);
        if (effect != null) {
            effect.end().run();
        }
    }

    public void clear() {
        for (Effect effect : this.active.values()) {
            effect.end().run();
        }
        this.active.clear();
    }
}
