package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.exprs.LiteralValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public record EvalContext(List<HashMap<String, LiteralValue>> scopes, HashMap<String, LiteralValue> globalScope,
                          AtomicBoolean shouldStop) {

    public static EvalContext create(HashMap<String, LiteralValue> globalScope, AtomicBoolean shouldStop) {
        var scopes = new ArrayList<HashMap<String, LiteralValue>>();
        scopes.add(new HashMap<>());
        return new EvalContext(scopes, globalScope, shouldStop);
    }

    public void enterScope() {
        scopes.add(new HashMap<>());
    }

    public void exitScope() {
        scopes.removeLast();
    }

    public void forLoopScope(String key, LiteralValue value) {
        var map = new HashMap<String, LiteralValue>();
        map.put(key, value);
        scopes.add(map);
    }

    public Optional<LiteralValue> lookup(String key) {
        for (var scope : scopes.reversed()) {
            var element = scope.get(key);
            if (element != null) return Optional.of(element);
        }
        return Optional.ofNullable(globalScope.get(key));
    }
}
