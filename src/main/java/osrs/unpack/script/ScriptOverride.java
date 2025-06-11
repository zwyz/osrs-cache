package osrs.unpack.script;

import osrs.unpack.Type;

import java.util.List;

public record ScriptOverride(List<Type> parameters, List<Type> results) {
}
