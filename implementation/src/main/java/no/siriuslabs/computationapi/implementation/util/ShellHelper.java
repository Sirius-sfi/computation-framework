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

/**
 * Helper class offering shell-/console-based services like calling command line programs with parameters.
 * This includes sending a list of follow-up commands to a program started by this helper (although without being able to return the console state to the caller inbetween calls).
 */
public class ShellHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShellHelper.class);

	/**
	 * Default constructor.
	 */
	private ShellHelper() {
	}

	/**
	 * Runs a shell command or program with the given parameters and follow-up commands.<p>
	 * The command and its parameters must match the correct pattern and format for the operating system the node is run on.<p>
	 * Usually the command should only be one "word" and the very first part of a call. E.g. (comments in []):
	 * <ul>
	 *     <li>Python: python3 [command] scriptName scriptParameter1 scriptParameter2 [commandParameters]</li>
	 *     <li>Java: java [command] -jar jarName programParameter [commandParameters]</li>
	 * </ul>
	 * Follow-up commands are intended for programs or commands that expect further input after the first call, but do not accept this input right away.
	 * This could be a copy command that expects a confirmation after being called. The workflow would look like this:<p>
	 * copy [command] sourceFile destinationFile [commandParameters] --> [execution] --> Y [confirmation at runtime] --> [end of program]<p>
	 * Resulting ShellHelper method call:<p>
	 * copy [command] sourceFile destinationFile [commandParameters] Y [followUpCommands]
	 * @param command           The command or program to be called. May also include path information e.g. "./subdir/command"
	 * @param commandParameters	List of parameters for the called program. See method description for examples to differentiate command and parameters.
	 * @param followUpCommands  Optional follow-up commands. See method description for explanation.
	 * @return	Returns the complete console output from executing the command and all follow-up commands until termination of the call.
	 */
	public static String runShellCommand(String command, List<String> commandParameters, String... followUpCommands) {
		return runShellCommand(null, command, commandParameters, followUpCommands);
	}

	/**
	 Runs a shell command or program with the given parameters and follow-up commands.<p>
	 * The command and its parameters must match the correct pattern and format for the operating system the node is run on.<p>
	 * Usually the command should only be one "word" and the very first part of a call. E.g. (comments in []):
	 * <ul>
	 *     <li>Python: python3 [command] scriptName scriptParameter1 scriptParameter2 [commandParameters]</li>
	 *     <li>Java: java [command] -jar jarName programParameter [commandParameters]</li>
	 * </ul>
	 * @param workingDirectory  Specifies the directory the command should be run in.
	 * @param command			The command or program to be called. May also include path information e.g. "./subdir/command"
	 * @param commandParameters	List of parameters for the called program. See method description for examples to differentiate command and parameters.
	 * @return	Returns the complete console output from executing the command until the termination of the call.
	 */
	public static String runShellCommand(File workingDirectory, String command, List<String> commandParameters) {
		return runShellCommand(workingDirectory, command, commandParameters, null);
	}

	/**
	 * Runs a shell command or program with the given parameters and follow-up commands.<p>
	 * The command and its parameters must match the correct pattern and format for the operating system the node is run on.<p>
	 * Usually the command should only be one "word" and the very first part of a call. E.g. (comments in []):
	 * <ul>
	 *     <li>Python: python3 [command] scriptName scriptParameter1 scriptParameter2 [commandParameters]</li>
	 *     <li>Java: java [command] -jar jarName programParameter [commandParameters]</li>
	 * </ul>
	 * @param command			The command or program to be called. May also include path information e.g. "./subdir/command"
	 * @param commandParameters	List of parameters for the called program. See method description for examples to differentiate command and parameters.
	 * @return	Returns the complete console output from executing the command until the termination of the call.
	 */
	public static String runShellCommand(String command, List<String> commandParameters) {
		return runShellCommand(null, command, commandParameters, null);
	}

	/**
	 * Runs a shell command or program with the given parameters and follow-up commands.<p>
	 * The command and its parameters must match the correct pattern and format for the operating system the node is run on.<p>
	 * Usually the command should only be one "word" and the very first part of a call. E.g. (comments in []):
	 * <ul>
	 *     <li>Python: python3 [command] scriptName scriptParameter1 scriptParameter2 [commandParameters]</li>
	 *     <li>Java: java [command] -jar jarName programParameter [commandParameters]</li>
	 * </ul>
	 * Follow-up commands are intended for programs or commands that expect further input after the first call, but do not accept this input right away.
	 * This could be a copy command that expects a confirmation after being called. The workflow would look like this:<p>
	 * copy [command] sourceFile destinationFile [commandParameters] --> [execution] --> Y [confirmation at runtime] --> [end of program]<p>
	 * Resulting ShellHelper method call:<p>
	 * copy [command] sourceFile destinationFile [commandParameters] Y [followUpCommands]
	 * @param workingDirectory  Specifies the directory the command should be run in.
	 * @param command			The command or program to be called. May also include path information e.g. "./subdir/command"
	 * @param commandParameters	List of parameters for the called program. See method description for examples to differentiate command and parameters.
	 * @param followUpCommands  Optional follow-up commands. See method description for explanation.
	 * @return	Returns the complete console output from executing the command and all follow-up commands until termination of the call.
	 */
	public static String runShellCommand(File workingDirectory, String command, List<String> commandParameters, String... followUpCommands) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);

		LOGGER.info("Directory parameter = {}", workingDirectory);
		if(workingDirectory != null) {
			LOGGER.info("Setting process directory to {}", workingDirectory.getAbsolutePath());
			processBuilder.directory(workingDirectory);
		}

		List<String> completeCallList = new ArrayList<>(commandParameters);
		completeCallList.add(0, command);
		String[] completeCall = completeCallList.toArray(new String[0]);

		LOGGER.info("Executing command: {}", completeCallList);
		processBuilder.command(completeCall);

		StringBuilder commandResult = new StringBuilder();
		try {
			Process process = processBuilder.start();

			if(followUpCommands != null && followUpCommands.length > 0) {
				LOGGER.info("{} follow-up commands found", followUpCommands.length);
				try(BufferedWriter shellInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
					for(String c : followUpCommands) {
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
