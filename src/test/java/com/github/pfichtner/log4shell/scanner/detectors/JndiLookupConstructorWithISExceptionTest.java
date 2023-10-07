package com.github.pfichtner.log4shell.scanner.detectors;

import static com.github.pfichtner.log4shell.scanner.DetectionCollector.Detection.getFormatted;
import static com.github.pfichtner.log4shell.scanner.util.Util.analyse;
import static com.github.pfichtner.log4shell.scanner.util.Util.withDetections;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.github.pfichtner.log4shell.scanner.DetectionCollector;
import com.github.pfichtner.log4shell.scanner.util.Log4jJars;

public class JndiLookupConstructorWithISExceptionTest {

	Log4jJars log4jJars = Log4jJars.getInstance();

	JndiLookupConstructorWithISException sut = new JndiLookupConstructorWithISException();

	@Test
	void throwingISEinJndiLookupConstructorWasIntroducedWIthLog4J217() throws Exception {
		DetectionCollector detector = new DetectionCollector(sut);
		String expected = "JNDI must be enabled by setting log4j2.enableJndiLookup=true access "
				+ "found in class org.apache.logging.log4j.core.lookup.JndiLookup";
		assertAll(
				() -> assertThat(getFormatted(detector.analyze(log4jJars.version("2.12.3").getAbsolutePath())))
						.singleElement(as(STRING)).startsWith(expected), //
				() -> assertThat(getFormatted(detector.analyze(log4jJars.version("2.17.0").getAbsolutePath())))
						.singleElement(as(STRING)).startsWith(expected) //
		);
	}

	@Test
	void canDetectAccess() throws Exception {
		assertThat(withDetections(analyse(log4jJars, sut))).containsOnlyKeys(log4jJars
				.versions("2.12.3", "2.17.0", "2.17.1", "2.17.2", "2.18.0", "2.19.0", "2.20.0").toArray(File[]::new));
	}

}
