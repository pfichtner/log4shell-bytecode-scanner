package com.github.pfichtner.log4shell.scanner.util;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class AsmUtil {

	private AsmUtil() {
		super();
	}

	public static boolean isClass(Path filename) {
		return filename.getFileSystem().getPathMatcher("glob:**.class").matches(filename);
	}

	public static ClassNode readClass(byte[] bytes, int options) {
		ClassNode classNode = new ClassNode();
		new ClassReader(bytes).accept(classNode, options);
		return classNode;
	}

	public static Map<Object, Object> toMap(AnnotationNode annotationNode, List<Object> values) {
		return IntStream.range(0, values.size() / 2).boxed().collect(
				Collectors.toMap(i -> annotationNode.values.get(i * 2), i -> annotationNode.values.get(i * 2 + 1)));
	}

	public static <T> List<T> nullSafety(List<T> list) {
		return list == null ? Collections.emptyList() : list;
	}

	public static String methodName(MethodInsnNode node) {
		Type methodType = Type.getMethodType(node.desc);
		String className = Type.getObjectType(node.owner).getClassName();
		String args = Arrays.stream(methodType.getArgumentTypes()).map(Type::getClassName).collect(joining(","));
		return className + "#" + node.name + "(" + args + ")";
	}

	public static Stream<MethodInsnNode> methodInsnNodes(ClassNode classNode, Predicate<MethodNode> methodFilter) {
		return methodInsnNode(
				classNode.methods.stream().filter(methodFilter).map(AsmUtil::instructionsStream).flatMap(identity()));
	}

	public static Stream<MethodInsnNode> methodInsnNode(Stream<AbstractInsnNode> instructions) {
		return Streams.filter(instructions, MethodInsnNode.class);
	}

	public static Stream<AbstractInsnNode> instructionsStream(MethodNode methodNode) {
		return Streams.itToStream(methodNode.instructions.iterator());
	}

	public static Stream<AbstractInsnNode> instructionsStream(InsnList instructions) {
		return Streams.itToStream(instructions.iterator());
	}

}