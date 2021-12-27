package com.github.pfichtner.log4shell.scanner.detectors;

import static com.github.pfichtner.log4shell.scanner.util.AsmTypeComparator.typeComparator;
import static com.github.pfichtner.log4shell.scanner.util.AsmUtil.methodInsnNodes;
import static com.github.pfichtner.log4shell.scanner.util.LookupConstants.JNDI_LOOKUP_TYPE;
import static com.github.pfichtner.log4shell.scanner.util.LookupConstants.LOOKUP_NAME;
import static com.github.pfichtner.log4shell.scanner.util.LookupConstants.namingContextLookup;

import java.nio.file.Path;

import org.objectweb.asm.tree.ClassNode;

/**
 * org/apache/logging/log4j/core/lookup/JndiLookup#lookup -->
 * javax/naming/Context#lookup
 */
public class NamingContextLookupCallsFromJndiLookup extends AbstractDetector {

	@Override
	public void visitClass(Path filename, ClassNode classNode) {
		if (typeComparator().isClass(classNode, JNDI_LOOKUP_TYPE)) {
			methodInsnNodes(classNode, n -> typeComparator().methodNameIs(n, LOOKUP_NAME)).filter(namingContextLookup())
					.forEach(n -> addDetection(filename, classNode, referenceTo(n)));
		}
	}

}
