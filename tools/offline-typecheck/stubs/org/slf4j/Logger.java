package org.slf4j;
public interface Logger {
  void info(String msg);   void info(String fmt, Object... a);
  void warn(String msg);   void warn(String fmt, Object... a);
  void error(String msg);  void error(String fmt, Object... a);
  void debug(String msg);  void debug(String fmt, Object... a);
}
