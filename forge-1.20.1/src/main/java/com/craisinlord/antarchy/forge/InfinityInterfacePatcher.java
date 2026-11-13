package com.craisinlord.antarchy.forge;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public final class InfinityInterfacePatcher {
    private static final String HOOKS = "com/craisinlord/antarchy/forge/InfinityInterfaceHooks";
    static final String PORTAL_CREATOR = "net/lerariemann/infinity/util/teleport/PortalCreator";
    static final String WARP_LOGIC = "net/lerariemann/infinity/util/teleport/WarpLogic";
    static final String CONFIG_GENERATOR = "net/lerariemann/infinity/util/config/ConfigGenerator";
    static final String INFINITY_METHODS = "net/lerariemann/infinity/util/InfinityMethods";
    private static final String ITEM_ENTITY = "net/minecraft/world/entity/item/ItemEntity";
    private static final String TRY_ADD_DESC = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/resources/ResourceLocation;)Z";

    private InfinityInterfacePatcher() {
    }

    public static void patch(ClassNode classNode) {
        switch (classNode.name) {
            case PORTAL_CREATOR -> patchPortalCreator(classNode);
            case WARP_LOGIC -> patchWarpLogic(classNode);
            case CONFIG_GENERATOR -> patchConfigGenerator(classNode);
            case INFINITY_METHODS -> patchBiomeCheck(classNode);
            default -> {
            }
        }
    }

    private static void patchPortalCreator(ClassNode classNode) {
        MethodNode method = find(classNode, "tryCreatePortalFromItem",
                "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/item/ItemEntity;)V");
        if (method == null) {
            return;
        }
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && ITEM_ENTITY.equals(call.owner) && "()Z".equals(call.desc)) {
                method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                        "isRemovedOrBlockedBook", "(L" + ITEM_ENTITY + ";)Z", false));
                return;
            }
        }
    }

    private static void patchWarpLogic(ClassNode classNode) {
        MethodNode method = find(classNode, "requestWarp",
                "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceLocation;Z)V");
        if (method == null) {
            return;
        }
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC
                    && PORTAL_CREATOR.equals(call.owner) && "tryAddInfinityDimension".equals(call.name)
                    && TRY_ADD_DESC.equals(call.desc)) {
                method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                        "tryAddInfinityDimension", TRY_ADD_DESC, false));
                return;
            }
        }
    }

    private static void patchConfigGenerator(ClassNode classNode) {
        MethodNode method = find(classNode, "generateBEs", "()V");
        if (method == null) {
            return;
        }
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKEINTERFACE
                    && "java/util/Set".equals(call.owner) && "forEach".equals(call.name)
                    && "(Ljava/util/function/Consumer;)V".equals(call.desc)) {
                method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                        "scanBlockEntities", "(Ljava/util/Set;Ljava/util/function/Consumer;)V", false));
                return;
            }
        }
    }

    private static void patchBiomeCheck(ClassNode classNode) {
        MethodNode method = find(classNode, "isBiomeInfinity",
                "(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Z");
        if (method == null || method.instructions.size() == 0) {
            return;
        }
        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();
        LabelNode handler = new LabelNode();
        method.instructions.insert(start);
        method.instructions.add(end);
        method.instructions.add(handler);
        method.instructions.add(new InsnNode(Opcodes.POP));
        method.instructions.add(new InsnNode(Opcodes.ICONST_0));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
        method.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, "java/lang/RuntimeException"));
    }

    private static MethodNode find(ClassNode classNode, String name, String desc) {
        for (MethodNode method : classNode.methods) {
            if (name.equals(method.name) && desc.equals(method.desc)) {
                return method;
            }
        }
        return null;
    }
}
