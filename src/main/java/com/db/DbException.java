package com.db;

public class DbException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  // Constructor with message and cause (SQLException)
  public DbException(String msg, Throwable cause) {
    // Call the parent class constructor with the message and cause
    super(msg, cause);
  }

  // Existing constructor for a message only
  public DbException(String msg) {
    super(msg);
  }
}
