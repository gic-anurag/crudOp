package com.fadv.cspi.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class ResponseStatusPOJO {
	private boolean success;
	@JsonProperty("successMsg")
	private String message;
	private String code;
	@JsonProperty("response")
	private Object data;

	public ResponseStatusPOJO(boolean success, String message, Object data) {
		this.success = success;
		this.message = message;
		this.data = data;
	}

	public ResponseStatusPOJO(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public ResponseStatusPOJO(boolean success, String message, String code) {
		this.success = success;
		this.message = message;
		this.code = code;
	}

	public ResponseStatusPOJO(boolean success, String message, String code, Object data) {
		this.success = success;
		this.message = message;
		this.code = code;
		this.data = data;
	}

	public String toString() {
		return "ResponseStatusBean [success=" + this.success + ", message=" + this.message + ", code=" + this.code
				+ ", data=" + this.data + "]";
	}
}
