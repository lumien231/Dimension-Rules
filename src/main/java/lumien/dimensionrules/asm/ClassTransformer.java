package lumien.dimensionrules.asm;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class ClassTransformer implements IClassTransformer
{
	Logger logger = LogManager.getLogger("DimensionRulesCore");

	final String asmHandler = "lumien/dimensionrules/RuleHandler";

	public ClassTransformer()
	{
		logger.log(Level.DEBUG, "Starting Class Transformation");
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (transformedName.equals("net.minecraft.world.storage.DerivedWorldInfo"))
		{
			return patchDerivedWorldInfo(basicClass);
		}
		else if (transformedName.equals("net.minecraft.world.World"))
		{
			return patchWorld(basicClass);
		}
		return basicClass;
	}
	
	private byte[] patchWorld(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.DEBUG, "Found World Class: " + classNode.name);

		MethodNode getGameRules = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(MCPNames.method("func_82736_K")))
			{
				getGameRules = mn;
			}
		}

		if (getGameRules != null)
		{
			logger.log(Level.DEBUG, " - Found getGameRules");

			InsnList toInsert = new InsnList();
			LabelNode l0 = new LabelNode(new Label());
			
			toInsert.add(new VarInsnNode(ALOAD, 0));
			toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "getGameRuleInstance", "(Lnet/minecraft/world/World;)Lnet/minecraft/world/GameRules;", false));
			toInsert.add(new InsnNode(DUP));
			toInsert.add(new JumpInsnNode(IFNULL, l0));
			toInsert.add(new InsnNode(ARETURN));
			toInsert.add(l0);
			toInsert.add(new InsnNode(POP));
			getGameRules.instructions.insert(toInsert);
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchDerivedWorldInfo(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.DEBUG, "Found DerivedWorldInfo Class: " + classNode.name);

		MethodNode getGameRulesInstance = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(MCPNames.method("func_82574_x")))
			{
				getGameRulesInstance = mn;
			}
		}

		if (getGameRulesInstance != null)
		{
			logger.log(Level.DEBUG, " - Found getGameRulesInstance");

			InsnList toInsert = new InsnList();
			LabelNode l0 = new LabelNode(new Label());
			
			toInsert.add(new VarInsnNode(ALOAD, 0));
			toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "getGameRuleInstance", "(Lnet/minecraft/world/storage/DerivedWorldInfo;)Lnet/minecraft/world/GameRules;", false));
			toInsert.add(new InsnNode(DUP));
			toInsert.add(new JumpInsnNode(IFNULL, l0));
			toInsert.add(new InsnNode(ARETURN));
			toInsert.add(l0);
			toInsert.add(new InsnNode(POP));
			getGameRulesInstance.instructions.insert(toInsert);
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchDummyClass(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.DEBUG, "Found Dummy Class: " + classNode.name);

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);

		return writer.toByteArray();
	}
}
