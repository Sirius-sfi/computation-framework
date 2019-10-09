package no.siriuslabs.computationapi.implementation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class ShellHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShellHelper.class);

	private ShellHelper() {
	}

	public static String runShellCommand(String command, List<String> commandParameters, String... followUpComamnds) {
		return runShellCommand(null, command, commandParameters, followUpComamnds);
	}

	public static String runShellCommand(File directory, String command, List<String> commandParameters) {
		return runShellCommand(directory, command, commandParameters, null);
	}

	public static String runShellCommand(String command, List<String> commandParameters) {
		return runShellCommand(null, command, commandParameters, null);
	}

	public static String runShellCommand(File directory, String command, List<String> commandParameters, String... followUpComamnds) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);

		LOGGER.info("Directory parameter = {}", directory);
		if(directory != null) {
			LOGGER.info("Setting process directory to {}", directory.getAbsolutePath());
			processBuilder.directory(directory);
		}

		List<String> completeCallList = new ArrayList<>(commandParameters);
		completeCallList.add(0, command);
		String[] completeCall = completeCallList.toArray(new String[0]);

		LOGGER.info("Executing command: {}", completeCallList);
		processBuilder.command(completeCall);

		StringBuilder commandResult = new StringBuilder();
		try {
			Process process = processBuilder.start();

			if(followUpComamnds != null && followUpComamnds.length > 0) {
				LOGGER.info("{} follow-up commands found", followUpComamnds.length);
				try(BufferedWriter shellInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
					for(String c : followUpComamnds) {
						sendCommandToShell(shellInput, c);
					}
				}
			}

			try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while((line = reader.readLine()) != null) {
					LOGGER.info(line);
					commandResult.append(line);
					commandResult.append(System.lineSeparator());
				}
			}

			int returnCode = process.waitFor();
			LOGGER.info("Exited with return code : {}", returnCode);
		}
		catch (IOException | InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return commandResult.toString();
	}

	private static void sendCommandToShell(BufferedWriter shellInput, String command) throws IOException {
		LOGGER.info("Sending follow-up command to shell: {}", command);
		shellInput.write(command.toCharArray());
		shellInput.newLine();
		shellInput.flush();
	}

}
