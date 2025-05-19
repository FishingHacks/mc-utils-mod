package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.statements.Statement;

import java.util.List;

public record FunctionValue(String name, List<String> arguments, List<Statement> statements) {
}
