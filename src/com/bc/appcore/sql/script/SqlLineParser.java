/*
 * Copyright 2017 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.appcore.sql.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 24, 2017 9:33:37 PM
 */
public class SqlLineParser {

  private final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

  private final PrintWriter logWriter = null; //new PrintWriter(System.out);
  private final PrintWriter errorLogWriter = null; //new PrintWriter(System.err);

  private String delimiter;
  private final boolean fullLineDelimiter;

  public SqlLineParser() { 
    this(";", false);    
  }
  
  public SqlLineParser(String delimiter, boolean fullLineDelimiter) { 
    this.delimiter = Objects.requireNonNull(delimiter);
    this.fullLineDelimiter = fullLineDelimiter;
  }

  public List<String> parse(File file, String charsetName) throws IOException {
      return parse(new InputStreamReader(new FileInputStream(file), charsetName));
  }
  
  public List<String> parse(Reader reader) throws IOException {
    final List<String> sqlLines = new ArrayList<>();
    StringBuilder command = new StringBuilder();
    try (BufferedReader lineReader = this.getBufferedReader(reader)){
      String line;
      while ((line = lineReader.readLine()) != null) {
        command = handleLine(command, line, sqlLines);
      }
      checkForMissingLineTerminator(command);
    } catch (IOException e) {
      final String message = "Error executing: " + command;
      printlnError(message + ". Cause: " + e);
      throw new IOException(message, e);
    }
    return sqlLines.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableList(sqlLines);
  }

  public BufferedReader getBufferedReader(Reader reader) {
    final BufferedReader bufferedReader;
    if(reader instanceof BufferedReader) {
        bufferedReader = (BufferedReader)reader;
    }else{
        bufferedReader = new BufferedReader(reader);
    }
    return bufferedReader;
  }

  private void checkForMissingLineTerminator(StringBuilder command) {
    if (command != null && command.toString().trim().length() > 0) {
      throw new RuntimeException("Line missing end-of-line terminator (" + delimiter + ") => " + command);
    }
  }

  private StringBuilder handleLine(StringBuilder command, String line, List<String> buffer) throws UnsupportedEncodingException {
    String trimmedLine = line.trim();
    if (lineIsComment(trimmedLine)) {
        final String cleanedString = trimmedLine.substring(2).trim().replaceFirst("//", "");
        if(cleanedString.toUpperCase().startsWith("@DELIMITER")) {
            delimiter = cleanedString.substring(11,12);
            return command;
        }
      println(trimmedLine);
    } else if (commandReadyToExecute(trimmedLine)) {
      command.append(line.substring(0, line.lastIndexOf(delimiter)));
      command.append(LINE_SEPARATOR);
      println(command);
      buffer.add(command.toString());
      command.setLength(0);
    } else if (trimmedLine.length() > 0) {
      command.append(line);
      command.append(LINE_SEPARATOR);
    }
    return command;
  }

  private boolean lineIsComment(String trimmedLine) {
    return trimmedLine.startsWith("//") || trimmedLine.startsWith("--");
  }

  private boolean commandReadyToExecute(String trimmedLine) {
    // issue #561 remove anything after the delimiter
    return !fullLineDelimiter && trimmedLine.contains(delimiter) || fullLineDelimiter && trimmedLine.equals(delimiter);
  }

  private void println(Object o) {
    if (logWriter != null) {
      logWriter.println(o);
      logWriter.flush();
    }
  }

  private void printlnError(Object o) {
    if (errorLogWriter != null) {
      errorLogWriter.println(o);
      errorLogWriter.flush();
    }
  }
}
