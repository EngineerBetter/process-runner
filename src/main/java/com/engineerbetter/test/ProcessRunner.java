package com.engineerbetter.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessRunner implements Runnable
{
	private final ProcessBuilder builder;
	private Integer exitValue;
	private StringBuffer stdOutBuffer;
	private StringBuffer stdErrBuffer;
	private volatile boolean shouldEnd;
	private volatile boolean started;
	private volatile boolean ended;
	private volatile boolean failed;
	private Process process;


	public ProcessRunner(ProcessBuilder builder)
	{
		this.builder = builder;
		this.stdOutBuffer = new StringBuffer();
		this.stdErrBuffer = new StringBuffer();
	}


	@Override
	public void run()
	{
		try
		{
			process = builder.start();
			started = true;

			InputStream stdOut = process.getInputStream();
			InputStream stdErr = process.getErrorStream();
			InputStreamReader stdOutReader = new InputStreamReader(stdOut);
			InputStreamReader stdErrReader = new InputStreamReader(stdErr);
			BufferedReader bufferedStdOutReader = new BufferedReader(stdOutReader);
			BufferedReader bufferedStdErrReader = new BufferedReader(stdErrReader);

			String outLine = null;
			String errLine = null;

			while (
				!shouldEnd &&
				(
					(outLine = bufferedStdOutReader.readLine()) != null ||
					(errLine = bufferedStdErrReader.readLine()) != null
				)
			)
			{
				if(outLine != null)
				{
					stdOutBuffer.append(outLine).append(System.lineSeparator());
				}

				if(errLine != null)
				{
					stdErrBuffer.append(errLine).append(System.lineSeparator());
				}

				try
				{
					exitValue = process.exitValue();
					shouldEnd = true;
				}
				catch(IllegalThreadStateException e)
				{
					//this is normal, sadly
				}
			}

			ended = true;
			exitValue = process.exitValue();
		}
		catch (IOException e)
		{
			failed = true;
			ended = true;
		}
	}


	public void stop()
	{
		shouldEnd = true;
	}


	public boolean isFinished()
	{
		return started && ended;
	}

	public boolean isFailed()
	{
		return failed;
	}

	public String getStdOut()
	{
		return stdOutBuffer.toString();
	}

	public String getStdErr()
	{
		return stdErrBuffer.toString();
	}

	public Integer getExitCode()
	{
		return exitValue;
	}

}
