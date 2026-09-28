package com.mojang.brigadier;
@FunctionalInterface public interface Command<S> { int run(com.mojang.brigadier.context.CommandContext<S> ctx) throws Exception; }
