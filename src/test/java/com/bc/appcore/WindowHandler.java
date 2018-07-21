/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bc.appcore;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 17, 2017 3:57:11 PM
 */
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class LogWindow extends JFrame {
    
  private final JTextArea textArea = new JTextArea();

  public LogWindow() {
    super("Log Window");
    this.getContentPane().add(new JScrollPane(textArea));
  }

  public void showInfo(String data) {
    textArea.append(data);
    this.validate();
  }
}

public class WindowHandler extends Handler {

  private LogWindow window = null;

  private static WindowHandler handler = null;

  public WindowHandler() {
    final LogManager manager = LogManager.getLogManager();
    final String className = this.getClass().getName();
    final String level = manager.getProperty(className + ".level");
    setLevel(level != null ? Level.parse(level) : Level.INFO);
    setFormatter(new SimpleFormatter());
    if (window == null) {
      window = new LogWindow();
      window.setSize(
              this.getDimension(manager, className + ".width", 300), 
              this.getDimension(manager, className + ".height", 300));
      window.setVisible(true);
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }  
//System.out.println("@WindowHandler on:"+new Date()+", window: " +window);
  }
  
  private int getDimension(LogManager manager, String property, int defaultValue) {
    final String dimStr = manager.getProperty(property);
    final int dim = dimStr == null || dimStr.isEmpty() ? defaultValue : Integer.parseInt(dimStr);
    return dim;
  }
  
  public static synchronized WindowHandler getInstance() {
    if (handler == null) {
      handler = new WindowHandler();
    }
    return handler;
  }

  @Override
  public synchronized void publish(LogRecord record) {
    if (!isLoggable(record)) {
      return;
    }  
    final String message = getFormatter().format(record);
    window.showInfo(message);
  }

  @Override
  public void close() { }

  @Override
  public void flush() { }
}