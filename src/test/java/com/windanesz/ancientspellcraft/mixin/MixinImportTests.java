package com.windanesz.ancientspellcraft.mixin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Test for illegal imports")
public class MixinImportTests {

	@Test
	@DisplayName("Ensure only allowed imports are used in vanilla mixins")
	public void testImports() throws IOException {
		File directory = new File("src/main/java/com/windanesz/ancientspellcraft/mixin/minecraft");

		boolean foundIllegalImports = Files.walk(directory.toPath())
				.filter(Files::isRegularFile)
				.filter(file -> file.toString().endsWith(".java"))
				.anyMatch(file -> {
					try {
						List<String> lines = Files.lines(file).collect(Collectors.toList());
						for (String line : lines) {
							if (line.matches("^\\s*import\\s+(?!net\\.minecraft\\.|net\\.minecraftforge|org\\.spongepowered\\.|com\\.windanesz\\.ancientspellcraft\\.mixin\\.modrefs\\.).*")) {
								System.out.println("Illegal import found in file: " + file + ", import statement: " + line);
								return true;
							}
						}
						return false;
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
				});

		assertFalse(foundIllegalImports, "Illegal imports found in some files.");
	}
}
