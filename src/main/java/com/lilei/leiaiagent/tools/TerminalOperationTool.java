package com.lilei.leiaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;

/**
 * 终端操作工具类
 * provides a method to execute terminal commands with a timeout and capture both standard output and error streams.
 * It uses Java's ProcessBuilder to run commands in the terminal and captures the output.
 */
public class TerminalOperationTool {

    @Tool(description = "Execute a command in the terminal with timeout and error stream capture")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command,
                                         @ToolParam(description = "Timeout in seconds for command execution") int timeoutSeconds) {
        StringBuilder output = new StringBuilder();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = builder.start();

            Future<String> future = executor.submit(() -> {
                StringBuilder result = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                     BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                    while ((line = errorReader.readLine()) != null) {
                        result.append("ERROR: ").append(line).append("\n");
                    }
                }
                return result.toString();
            });

            output.append(future.get(timeoutSeconds, TimeUnit.SECONDS));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (TimeoutException e) {
            output.append("Command execution timed out after ").append(timeoutSeconds).append(" seconds.");
        } catch (IOException | InterruptedException | ExecutionException e) {
            output.append("Error executing command: ").append(e.getMessage());
        } finally {
            executor.shutdown();
        }
        return output.toString();
    }
}