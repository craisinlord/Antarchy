package com.craisinlord.antarchy.mixins;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

final class AntarchyRangedAttackPatcher {
    private static final String RANGED_ATTACK_DESC = "(Lnet/minecraft/world/entity/LivingEntity;F)V";
    private static final String LIVING_DESC = "(Lnet/minecraft/world/entity/LivingEntity;)D";
    private static final String LIVING_D_DESC = "(Lnet/minecraft/world/entity/LivingEntity;D)D";
    private static final String D_LIVING_DESC = "(DLnet/minecraft/world/entity/LivingEntity;)D";
    private static final String DELTA_MOVEMENT_DESC = "(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/phys/Vec3;";
    private static final String API_OWNER = "com/craisinlord/antarchy/content/gravity/AntarchyGravityApi";

    private enum AimMode {
        BODY,
        EYE
    }

    int patch(ClassNode classNode) {
        int total = 0;
        for (MethodNode method : classNode.methods) {
            if (!RANGED_ATTACK_DESC.equals(method.desc)) {
                continue;
            }
            if (!isPerformRangedAttack(method.name)) {
                continue;
            }
            if (!hasRangedAimSqrt(method)) {
                continue;
            }
            total += applyPatch(method);
        }
        return total;
    }

    private boolean isPerformRangedAttack(String methodName) {
        return "performRangedAttack".equals(methodName)
                || "m_6504_".equals(methodName)
                || "m_31448_".equals(methodName)
                || "m_31457_".equals(methodName);
    }

    private boolean hasRangedAimSqrt(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call
                    && call.getOpcode() == Opcodes.INVOKESTATIC
                    && "java/lang/Math".equals(call.owner)
                    && "sqrt".equals(call.name)
                    && "(D)D".equals(call.desc)) {
                return true;
            }
        }
        return false;
    }

    private AimMode detectAimMode(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call)) {
                continue;
            }
            int opcode = call.getOpcode();
            if (opcode != Opcodes.INVOKEVIRTUAL && opcode != Opcodes.INVOKEINTERFACE) {
                continue;
            }
            if (isGetYScaled(call)) {
                return AimMode.BODY;
            }
        }
        return AimMode.EYE;
    }

    private int applyPatch(MethodNode method) {
        AimMode mode = detectAimMode(method);
        int total = 0;
        int ordX = 0;
        int ordZ = 0;
        int ordYScaled = 0;
        int ordEyeY = 0;
        InsnList list = method.instructions;
        for (AbstractInsnNode cur = list.getFirst(); cur != null; cur = cur.getNext()) {
            if (!(cur instanceof MethodInsnNode call)) {
                continue;
            }
            int opcode = call.getOpcode();
            if (opcode != Opcodes.INVOKEVIRTUAL && opcode != Opcodes.INVOKEINTERFACE) {
                continue;
            }
            if (isGetX(call)) {
                if (ordX++ != 0) {
                    continue;
                }
                list.set(cur, new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        API_OWNER,
                        mode == AimMode.BODY ? "rangedBodyTargetX" : "rangedEyeTargetX",
                        LIVING_DESC,
                        false
                ));
                total++;
                continue;
            }
            if (isGetZ(call)) {
                if (ordZ++ != 0) {
                    continue;
                }
                list.set(cur, new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        API_OWNER,
                        mode == AimMode.BODY ? "rangedBodyTargetZ" : "rangedEyeTargetZ",
                        LIVING_DESC,
                        false
                ));
                total++;
                continue;
            }
            if (mode == AimMode.BODY && isGetYScaled(call)) {
                if (ordYScaled++ != 0) {
                    continue;
                }
                list.set(cur, new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        API_OWNER,
                        "rangedBodyTargetY",
                        LIVING_D_DESC,
                        false
                ));
                total++;
                continue;
            }
            if (mode == AimMode.EYE && isGetEyeY(call)) {
                if (ordEyeY++ != 0) {
                    continue;
                }
                list.set(cur, new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        API_OWNER,
                        "rangedEyeTargetY",
                        LIVING_DESC,
                        false
                ));
                total++;
            }
        }
        total += patchSqrtAndDelta(method);
        return total;
    }

    private int patchSqrtAndDelta(MethodNode method) {
        int total = 0;
        InsnList list = method.instructions;
        for (AbstractInsnNode cur = list.getFirst(); cur != null; cur = cur.getNext()) {
            if (!(cur instanceof MethodInsnNode call)) {
                continue;
            }
            if ((call.getOpcode() == Opcodes.INVOKEVIRTUAL || call.getOpcode() == Opcodes.INVOKEINTERFACE)
                    && isGetDeltaMovement(call)
                    && isAload1(previousSignificant(cur))) {
                list.set(cur, new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        API_OWNER,
                        "deltaMovement",
                        DELTA_MOVEMENT_DESC,
                        false
                ));
                total++;
                continue;
            }
            if (call.getOpcode() == Opcodes.INVOKESTATIC
                    && "java/lang/Math".equals(call.owner)
                    && "sqrt".equals(call.name)
                    && "(D)D".equals(call.desc)) {
                list.insertBefore(cur, new VarInsnNode(Opcodes.ALOAD, 1));
                list.set(cur, new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        API_OWNER,
                        "rangedSqrt",
                        D_LIVING_DESC,
                        false
                ));
                total++;
            }
        }
        return total;
    }

    private boolean isGetX(MethodInsnNode call) {
        return "()D".equals(call.desc) && ("getX".equals(call.name) || "m_20185_".equals(call.name));
    }

    private boolean isGetZ(MethodInsnNode call) {
        return "()D".equals(call.desc) && ("getZ".equals(call.name) || "m_20189_".equals(call.name));
    }

    private boolean isGetYScaled(MethodInsnNode call) {
        return "(D)D".equals(call.desc) && ("getY".equals(call.name) || "m_20227_".equals(call.name));
    }

    private boolean isGetEyeY(MethodInsnNode call) {
        return "()D".equals(call.desc) && ("getEyeY".equals(call.name) || "m_20188_".equals(call.name));
    }

    private boolean isGetDeltaMovement(MethodInsnNode call) {
        return "()Lnet/minecraft/world/phys/Vec3;".equals(call.desc)
                && ("getDeltaMovement".equals(call.name) || "m_20184_".equals(call.name));
    }

    private boolean isAload1(AbstractInsnNode insn) {
        return insn instanceof VarInsnNode varInsn && varInsn.getOpcode() == Opcodes.ALOAD && varInsn.var == 1;
    }

    private AbstractInsnNode previousSignificant(AbstractInsnNode node) {
        AbstractInsnNode previous = node.getPrevious();
        while (previous != null) {
            int type = previous.getType();
            if (type != AbstractInsnNode.LABEL && type != AbstractInsnNode.LINE) {
                return previous;
            }
            previous = previous.getPrevious();
        }
        return null;
    }
}
