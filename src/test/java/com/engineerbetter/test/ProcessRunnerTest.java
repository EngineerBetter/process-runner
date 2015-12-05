package com.engineerbetter.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.junit.Test;

import com.jayway.awaitility.Awaitility;

public class ProcessRunnerTest
{
	private final String[] cmd = {"cmd", "/c"};
	private final String[] bash = {"bash", "-c"};


	@Test
	public void invalidCommandFails() throws Exception
	{
		String[] command = {"willnotwork"};

		ProcessBuilder builder = new ProcessBuilder(command);
		ProcessRunner runner = new ProcessRunner(builder);
		runner.run();

		Awaitility.await().until(isFinished(runner));
		assertThat("process runner should have failed", runner.isFailed(), is(true));
	}


	@Test
	public void erringCommandReturnsNonZeroExitCode()
	{
		String[] command = shellCommand("willnotwork");

		ProcessBuilder builder = new ProcessBuilder(command);
		ProcessRunner runner = new ProcessRunner(builder);
		runner.run();

		Awaitility.await().until(isFinished(runner));
		assertThat("process runner should have finished", runner.isFinished(), is(true));
		assertThat("process runner should have succeeded", runner.isFailed(), is(false));
		assertThat(runner.getExitCode(), not(0));
	}


	@Test
	public void successfulCommandFinishesWithZero()
	{
		String[] command = listDirectory();

		ProcessBuilder builder = new ProcessBuilder(command);
		ProcessRunner runner = new ProcessRunner(builder);
		runner.run();

		Awaitility.await().until(isFinished(runner));
		assertThat("process should not have failed", runner.isFailed(), is(false));
		assertThat("process should have finished", runner.isFinished(), is(true));
		assertThat(runner.getExitCode(), is(0));
	}


	private String[] listDirectory()
	{
		return shellCommand(isWindows() ? "dir" : "ls");
	}


	private String[] shellCommand(String command)
	{
		String[] shell = isWindows() ? cmd : bash;
		return Stream.concat(Stream.of(shell), Stream.of(command)).toArray(String[]::new);
	}


	private boolean isWindows()
	{
		return System.getProperty("os.name").startsWith("Windows");
	}


	private Callable<Boolean> isFinished(final ProcessRunner runner)
	{
		return new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return runner.isFinished() || runner.isFailed();
			}
		 };
	}
}
