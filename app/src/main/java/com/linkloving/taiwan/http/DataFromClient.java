/**
 * DataFromServer.java
 * @author Jason Lu
 * @date 2014-2-27
 * @version 1.0
 */
package com.linkloving.taiwan.http;

/** @deprecated */
public class DataFromClient
{

	//方法ID
	private int actionId = -9999;

	//处理器ID
	private int jobDispatchId = -9999;

	//调度器ID
	private int processorId = -9999;

	//参数 以JSON形式传输  key,value
	private String newData;

	private String oldData;

	private boolean doInput = true;

	public int getActionId()
	{
		return actionId;
	}

	public void setActionId(int actionId)
	{
		this.actionId = actionId;
	}

	public int getJobDispatchId()
	{
		return jobDispatchId;
	}

	public void setJobDispatchId(int jobDispatchId)
	{
		this.jobDispatchId = jobDispatchId;
	}

	public int getProcessorId()
	{
		return processorId;
	}

	public void setProcessorId(int processorId)
	{
		this.processorId = processorId;
	}

	public String getNewData()
	{
		return newData;
	}

	public void setNewData(String newData)
	{
		this.newData = newData;
	}

	public String getOldData()
	{
		return oldData;
	}

	public void setOldData(String oldData)
	{
		this.oldData = oldData;
	}

	public boolean isDoInput()
	{
		return doInput;
	}

	public void setDoInput(boolean doInput)
	{
		this.doInput = doInput;
	}

}
