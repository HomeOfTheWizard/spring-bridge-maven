package com.homeofthewizard;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public final class FactoriesProcessor implements Processor {

	static final String INDEX_FOLDER = "META-INF/";

	static final String PLUGIN = "com.homeofthewizard.SpringBootPlugin";

	private final Map<Object, Set<String>> index = new LinkedHashMap<>();

	private ProcessingEnvironment environment;

	public void init(final ProcessingEnvironment _environment) {
		environment = _environment;
	}

	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment round) {
		for (final TypeElement anno : annotations) {
			for (final Element elem : round.getElementsAnnotatedWith(anno)) {
				if (elem.getKind().isClass()) {
					for (final AnnotationMirror mirror : elem.getAnnotationMirrors()) {
						if (PLUGIN.equals(mirror.getAnnotationType().toString())) {
							for (final Entry<? extends ExecutableElement, ?> entry : mirror
									.getElementValues().entrySet()) {
								if ("value".equals(entry.getKey().getSimpleName().toString())) {
									@SuppressWarnings("unchecked")
									List<AnnotationValue> value = (List<AnnotationValue>) ((AnnotationValue) entry
											.getValue()).getValue();
									for (AnnotationValue v : value) {
										addClassToIndex(PLUGIN, v.getValue());
									}
								}
							}
						}
					}
				}
			}
		}

		if (round.processingOver()) {
			flushIndex();
		}

		return false;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(PLUGIN);
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	public Iterable<? extends Completion> getCompletions(final Element element, final AnnotationMirror annotation,
														 final ExecutableElement member, final String userText) {
		return Collections.emptySet();
	}

	synchronized void flushIndex() {
		for (final Entry<Object, Set<String>> entry : index.entrySet()) {
			writeTable(entry.getKey(), entry.getValue());
		}
	}

	synchronized void addClassToIndex(final Object anno, final Object clazz) {
		Set<String> table = index.get(anno);
		if (null == table) {
			table = readTable(anno);
			index.put(anno, table);
		}
		table.add(String.valueOf(clazz));
		info("Adding " + clazz + " to " + anno);
	}

	private Set<String> readTable(Object anno) {
		Properties properties = getFactories();
		Set<String> result = new LinkedHashSet<>();
		String value = properties.getProperty(anno.toString());
		if (value != null) {
			for (String s : value.replaceAll("\\s", "").split(",")) {
				result.add(s.trim());
			}
		}
		return result;
	}

	private void writeTable(Object key, Set<String> value) {
		Properties properties = getFactories();
		StringBuilder existing = new StringBuilder();
		for (String s : value) {
			existing.append((!existing.isEmpty()) ? "," : "").append(s);
		}
		properties.setProperty(key.toString(), existing.toString());
		try (Writer writer = getWriter(INDEX_FOLDER + "spring.factories")) {
			properties.store(writer, "Updated by APT processor");
		} catch (IOException e) {
			warn("could not update spring.factories for " + PLUGIN + " annotated classes.");
		}
	}

	private Properties getFactories() {
		Properties properties = new Properties();
		try (Reader reader = getReader(INDEX_FOLDER + "spring.factories")) {
			properties.load(reader);
		} catch (IOException e) {
			warn("could not find spring.factories file in classpath.");
		}
		return properties;
	}

	private void info(final String msg) {
		environment.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
	}

	private void warn(final String msg) {
		environment.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
	}

	private void error(final String msg) {
		environment.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
	}

	private Reader getReader(final String path)
			throws IOException {
		final FileObject file = environment.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path);
		return new InputStreamReader(file.openInputStream(), StandardCharsets.UTF_8);
	}

	private Writer getWriter(final String path)
			throws IOException {
		FileObject resource = environment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path);
		new File(resource.toUri()).getParentFile().mkdirs();
		return resource.openWriter();
	}

}
