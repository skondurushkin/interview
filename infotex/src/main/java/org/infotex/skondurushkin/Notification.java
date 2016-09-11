package org.infotex.skondurushkin;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.infotex.skondurushkin.factorizer.Factor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Notification {

	static final String NO_MESSAGE = "<no message>";
	@JsonProperty("task_id")
	long		taskId;
	@JsonProperty("type")
	String  	type;
	@JsonProperty("content")
	@JsonInclude(value = Include.NON_NULL)
	JsonNode	content;
	
	public Notification() {
		
	}
	protected Notification(long taskId, String type) {
		this.taskId = taskId;
		this.type = type;
		this.content = null; 
	}
	
	public long getTaskId() {
		return this.taskId;
	}
	public String getType() {
		return this.type;
	}
	private static class SuccessNotification extends Notification {
		SuccessNotification(long taskId, String status) {
			super(taskId, "success");
			this.content = JsonNodeFactory.instance.objectNode();
			((ObjectNode)this.content).set("status", JsonNodeFactory.instance.textNode(status));
		}
	}
	private static class AddedNotification extends SuccessNotification {
		AddedNotification(long taskId) {
			super(taskId, "added");
		}
	}
	private static class RunningNotification extends SuccessNotification {
		RunningNotification(long taskId) {
			super(taskId, "running");
		}
	}
	private static class RemovedNotification extends SuccessNotification {
		RemovedNotification(long taskId) {
			super(taskId, "removed");
		}
	}
	private static class FailedNotification extends SuccessNotification {
		FailedNotification(long taskId) {
			super(taskId, "failed");
		}
	}
	private static class DoneNotification extends SuccessNotification {
		DoneNotification(long taskId, JsonNode result) {
			super(taskId, "done");
			((ObjectNode)this.content).set("result", result);
		}
	}
	
	private static class ErrorNotification  extends Notification {
		ErrorNotification(long taskId, String message, Throwable exception) {
			super(taskId, "error");
			StringBuilder sb = new StringBuilder(StringUtils.defaultIfBlank(message, NO_MESSAGE));
			if (exception != null) {
				sb
					.append("Exception caught: ")
					.append(exception.getClass().getName())
					.append("\n")
					.append(exception.getMessage());
			} else  if (sb.length() == 0) {
				sb.append("\n<no exception>");
			}
			this.content = JsonNodeFactory.instance.textNode(sb.toString());
		}
		@Override
		public JsonNode getContent() {
			return this.content;
		}
	}
	public JsonNode getContent() {
		return this.content;
	}
	// builders
	public static Notification exception(long taskId, String message, Throwable exception) {
		return new ErrorNotification(taskId, message, exception);
	}
	public static Notification added(long taskId) {
		return new AddedNotification(taskId);
	}
	public static Notification removed(long taskId) {
		return new RemovedNotification(taskId);
	}
	public static Notification done(long taskId, List<Factor> data) {
		return new DoneNotification(taskId, Helpers.asJsonArray(data));
	}
	public static Notification running(long taskId) {
		return new RunningNotification(taskId);
	}
	public static Notification failed(long taskId) {
		return new FailedNotification(taskId);
	}
}
