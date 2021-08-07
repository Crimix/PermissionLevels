//package com.black_dog20.permissionlevels.mixin;
//
//import com.mojang.brigadier.CommandDispatcher;
//import net.minecraft.commands.CommandSourceStack;
//import net.minecraft.server.commands.OpCommand;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(OpCommand.class)
//public class OpCommandMixin {
//
//    @Inject(method = "register(Lcom/mojang/brigadier/CommandDispatcher;)V", at = @At("HEAD"), cancellable = true)
//    private static void register(CommandDispatcher<CommandSourceStack> source, CallbackInfo ci) {
//        ci.cancel();
//    }
//} TODO When mixins are back
