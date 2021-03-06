import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public abstract class MergeSourcesAction extends AnAction {

	public MergeSourcesAction(String text) {
		super(text);
	}

	protected Optional<MergedFile> collectMergedFile(Project project) {
		File baseDir = Paths.get(project.getBasePath(), "src").toFile();
		List<SourceFile> sourceFiles = getFilesIn(baseDir)
				.map(this::parseSourceFile)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());

		if(sourceFiles.isEmpty()) {
			Messages.showMessageDialog(project, "No source files found!", "Merge Sources", Messages.getErrorIcon());
			return Optional.empty();
		}
		if(!allSourceFilesOfSameType(sourceFiles)) {
			Messages.showMessageDialog(project, "Found source files of different types!", "Merge Sources", Messages.getErrorIcon());
			return Optional.empty();
		}

		return Optional.of(new MergedFile(sourceFiles));
	}

	private Stream<File> getFilesIn(File directory) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(directory + " is not a directory");
		}

		return Arrays.stream(directory.listFiles())
				.flatMap(file -> file.isDirectory() ? getFilesIn(file) : Stream.of(file));
	}

	private Optional<SourceFile> parseSourceFile(File file) {
		if(!file.isFile()) {
			throw new IllegalArgumentException(file + " is not a file");
		}

		if(file.getName().endsWith(".kt")) {
			return Optional.of(KotlinSourceFile.parse(file));
		}

		return Optional.empty();
	}

	private boolean allSourceFilesOfSameType(List<SourceFile> sourceFiles) {
		Class<? extends SourceFile> expectedType = sourceFiles.get(0).getClass();
		return sourceFiles.stream()
				.allMatch(sourceFile -> sourceFile.getClass() == expectedType);
	}
}
