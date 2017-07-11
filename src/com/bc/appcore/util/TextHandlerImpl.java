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

package com.bc.appcore.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 25, 2017 8:17:32 PM
 */
public class TextHandlerImpl implements TextHandler {
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat();
    
    private final String [] dateFormatPatterns = {
        "dd MMM yy",   "dd MMM, yy",   "MMM dd yy",   "MMM dd, yy",   "dd MM yy",   "dd MM, yy",
        "dd MMM yyyy", "dd MMM, yyyy", "MMM dd yyyy", "MMM dd, yyyy", "dd MM yyyy", "dd MM, yyyy",
        
        "dd-MMM-yy",   "dd-MMM, yy",   "MMM-dd-yy",   "MMM-dd, yy",   "dd-MM-yy",   "dd-MM, yy",
        "dd-MMM-yyyy", "dd-MMM, yyyy", "MMM-dd-yyyy", "MMM-dd, yyyy", "dd-MM-yyyy", "dd-MM, yyyy",
        
        "dd/MMM/yy",   "dd/MMM, yy",   "MMM/dd/yy",   "MMM/dd, yy",   "dd/MM/yy",   "dd/MM, yy",
        "dd/MMM/yyyy", "dd/MMM, yyyy", "MMM/dd/yyyy", "MMM/dd, yyyy", "dd/MM/yyyy", "dd/MM, yyyy",
    };
    
    private final String months = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec";
    
    private final Pattern monthsPattern = Pattern.compile(months, Pattern.CASE_INSENSITIVE);
    
    private final Pattern datePattern0 = Pattern.compile("\\d{1,2}\\s*?\\p{Punct}*?\\s*?("+months+")\\s*?\\p{Punct}*?\\s*?(\\d{4}|\\d{2})", Pattern.CASE_INSENSITIVE);
    private final Pattern datePattern1 = Pattern.compile("("+months+")\\s*?\\p{Punct}*?\\s*?\\d{1,2}\\s*?\\p{Punct}*?\\s*?(\\d{4}|\\d{2})", Pattern.CASE_INSENSITIVE);
    private final Pattern [] datePatterns = {datePattern0, datePattern1};
    
    private final Pattern dashWithSurroundingSpaces = Pattern.compile("(\\s+?(-)|(-)\\s+?)");
    
    private final Pattern punctPattern = Pattern.compile("\\p{Punct}");
    
    public TextHandlerImpl() { }
    
//    public static void main(String [] args) {
//        TextHandlerImpl th = new TextHandlerImpl();
//        final String cellContents = "NAF/527/LOG/COMMS dated 5 Oct 16";
//        final String trackStartStr = th.isNullOrEmpty(cellContents) ? null : th.getLastDateStr(cellContents);
//System.out.println(trackStartStr);        
//        Date timeopened = th.getDate(trackStartStr);
//System.out.println(timeopened);
//    }
    
    @Override
    public int indexOfMatchingText(String [] arr, String toMatch) {
        if(toMatch == null || toMatch.isEmpty()) {
            return -1;
        }
        for(int i=0; i<arr.length; i++) {
            String toMatchX = toMatch.replaceAll("\\s", "");
            String arrX = arr[i].replaceAll("\\s", "");
            if(toMatch.equalsIgnoreCase(arr[i]) || toMatch.toUpperCase().contains(arr[i]) || 
                    toMatchX.equalsIgnoreCase(arrX) || toMatchX.toUpperCase().contains(arrX)) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public String getLastDateStr(String str) {
        List<String> dateStrList = this.getDateStrList(str);
        return dateStrList.isEmpty() ? null : dateStrList.get(dateStrList.size() - 1);
    }
    
    @Override
    public List<String> getDateStrList(String str) {
        List<String> output = new ArrayList<>();
        for(Pattern datePattern : datePatterns) {
            Matcher m = datePattern.matcher(str);
            while(m.find()) {
                String dateStr = m.group();
                output.add(dateStr);
            }
        }
        return output;
    }
    
    @Override
    public List<Date> getDateList(String str) {
        List<Date> output = new ArrayList<>();
        List<String> dateStrList = this.getDateStrList(str);
        for(String dateStr : dateStrList) {
            Date date = this.getDate(dateStr);
            if(date == null) {
                continue;
            }
            output.add(this.getDate(dateStr));
        }
        return output;
    }

    @Override
    public Date getDate(String str) {
        Date date = this.doGetDate(str);
        if(!this.isNullOrEmpty(str) && date== null) {
            str = this.removeMultipleSpaces(str);
            if(!this.punctPattern.matcher(str).find()) {
                str = this.addSpaceAfterFirst2Digits(str);
                str = this.addSpaceAfterMonthPart(str);
            }
            str = this.removeSpacesAroundDashes(str);
            date = this.doGetDate(str);
        }
        return date;
    }
    
    private Date doGetDate(String str) {
        if(str == null || str.isEmpty()) {
            return null;
        }
        for(String dateFormatPattern : this.dateFormatPatterns) {
            try{
                this.dateFormat.applyPattern(dateFormatPattern);
                Date date = dateFormat.parse(str);
                if(date != null) {
                    return date;
                }
            }catch(ParseException ignored) { }
        }
        return null;
    }
    
    @Override
    public boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    @Override
    public String removeMultipleSpaces(String str) {
        return str.replaceAll("\\s{2,}", " ");
    }   
    
    @Override
    public String addSpaceAfterFirst2Digits(String str) {
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<str.length(); i++) {
            final char ch = str.charAt(i);
            if(i == 2 && !Character.isWhitespace(ch)) {
                builder.append(' ');
            }
            builder.append(ch);
        }
        return builder.toString();
    }
    @Override
    public String addSpaceAfterMonthPart(final String str) {
        final Matcher m = this.monthsPattern.matcher(str);
        final StringBuffer sb = new StringBuffer();
        final boolean found;
        if((found = m.find())) {
//System.out.println("Found: "+m.group());            
            m.appendReplacement(sb, m.group()+' ');
        }
        final String output;
        if(found) {
            m.appendTail(sb);
            output = sb.toString();
        }else{
            output = str;
        }
//System.out.println("In: "+str+", out: "+output);        
        return output;
    }
    
    @Override
    public String removeSpacesAroundDashes(String str) {
        while(true) {
            String update = this.doRemoveSpacesAroundDashes(str);
            if(update.length() == str.length()) {
                break;
            }
            str = update;        
        }
        return str;
    }
    
    private String doRemoveSpacesAroundDashes(String str) {
        final Matcher m = this.dashWithSurroundingSpaces.matcher(str);
        final StringBuffer buff = new StringBuffer();
        while(m.find()) {
            m.appendReplacement(buff, m.group().trim());
        }
        m.appendTail(buff);
        return buff.toString();
    }
}
/**
 * 
    public String removeMultipleSpaces_old(String str) {
        while(true) {
            int len = str.length();
            str = str.replace("  ", " ");
            if(len == str.length()) {
                break;
            }
        }
        return str;
    }
 * 
 */