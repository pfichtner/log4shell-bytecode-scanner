package com.github.pfichtner.log4shell.scanner;

import static com.github.pfichtner.log4shell.scanner.util.AsmUtil.isClass;
import static com.github.pfichtner.log4shell.scanner.util.AsmUtil.readClass;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;

import com.github.pfichtner.log4shell.scanner.CVEDetector.Detections.Detection;
import com.github.pfichtner.log4shell.scanner.io.Detector;
import com.github.pfichtner.log4shell.scanner.io.JarReader;
import com.github.pfichtner.log4shell.scanner.io.JarReader.JarReaderVisitor;

public class CVEDetector {

	private List<Detector<Detections>> detectors;

	public static class Detections {

		public static class Detection {

			private final Detector<?> detector;
			private final Path filename;
			private final Object object;

			public Detection(Detector<?> detector, Path filename, Object object) {
				this.detector = detector;
				this.filename = filename;
				this.object = object;
			}

			public String format() {
				return detector.format(this) + " found in class " + filename;
			}

			public Detector<?> getDetector() {
				return detector;
			}

			public Object getObject() {
				return object;
			}

		}

		private final List<Detection> detections = new ArrayList<>();

		public void add(Detector<?> detector, Path filename) {
			add(detector, filename, null);
		}

		public void add(Detector<?> detector, Path filename, Object object) {
			this.detections.add(new Detection(detector, filename, object));
		}

		public List<Detection> getDetections() {
			return detections;
		}

		public List<String> getFormatted() {
			return detections.stream().map(Detection::format).collect(toList());
		}

	}

	@SafeVarargs
	public CVEDetector(Detector<Detections>... detectors) {
		this(Arrays.asList(detectors));
	}

	public CVEDetector(List<Detector<Detections>> detectors) {
		this.detectors = unmodifiableList(new ArrayList<>(detectors));
	}

	public List<Detector<Detections>> getDetectors() {
		return detectors;
	}

	public void check(String jar) throws IOException {
		check(new File(jar));
	}

	public void check(File file) throws IOException {
		for (Detection detection : analyze(file).getDetections()) {
			System.out.println(detection.format());
		}
	}

	public Detections analyze(String jar) throws IOException {
		return analyze(new File(jar));
	}

	public Detections analyze(File jar) throws IOException {
		Detections detections = new Detections();
		new JarReader(jar).accept(new JarReaderVisitor() {
			@Override
			public void visitFile(Path file, byte[] bytes) {
				if (isClass(file)) {
					ClassNode classNode = readClass(bytes, 0);
					for (Detector<Detections> detector : detectors) {
						detector.visitClass(detections, file, classNode);
					}
				} else {
					for (Detector<Detections> detector : detectors) {
						detector.visitFile(detections, file, bytes);
					}
				}
			}
		});
		return detections;
	}

}
