package com.engineerbetter.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.Callable;

import org.junit.Test;

import com.jayway.awaitility.Awaitility;

public class ProcessRunnerTest
{
	@Test
	public void invalidCommandFails() throws Exception
	{
		String[] command = {"LAME"};

		ProcessBuilder builder = new ProcessBuilder(command);
		ProcessRunner runner = new ProcessRunner(builder);
		runner.run();

		Awaitility.await().until(isFinished(runner));
		assertThat("process runner should have failed", runner.isFailed(), is(true));
	}


	@Test
	public void erringCommandReturnsNonZeroExitCode()
	{
		String[] command = {"cmd", "/c", "LAME"};

		ProcessBuilder builder = new ProcessBuilder(command);
		ProcessRunner runner = new ProcessRunner(builder);
		runner.run();

		Awaitility.await().until(isFinished(runner));
		assertThat("process runner should have succeeded", runner.isFinished(), is(true));
		assertThat("process runner should have succeeded", runner.isFailed(), is(false));
		assertThat(runner.getExitCode(), is(1));
	}

	@Test
	public void successfulCommandFinishesWithZero()
	{
		String[] command = {"cmd", "/c", "dir"};

		ProcessBuilder builder = new ProcessBuilder(command);
		ProcessRunner runner = new ProcessRunner(builder);
		runner.run();

		Awaitility.await().until(isFinished(runner));
		assertThat("process should not have failed", runner.isFailed(), is(false));
		assertThat("process should have finished", runner.isFinished(), is(true));
		assertThat(runner.getExitCode(), is(0));
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
