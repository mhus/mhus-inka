package de.mhu.com.morse.channel.exec;

public class ExecException extends Exception {

	private int ec;

	public ExecException( int errorCode ) {
		ec = errorCode;
	}

	public ExecException(int errorCode , String message) {
		super(message);
		ec = errorCode;
	}

	public ExecException(int errorCode , Throwable cause) {
		super(cause);
		ec = errorCode;
	}

	public ExecException(int errorCode , String message, Throwable cause) {
		super(message, cause);
		ec = errorCode;
	}
	
	public int getErrorCode() {
		return ec;
	}

}
