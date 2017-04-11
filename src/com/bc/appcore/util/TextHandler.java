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

import java.util.Date;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 4, 2017 10:49:46 AM
 */
public interface TextHandler {

    String addSpaceAfterFirst2Digits(String str);

    String addSpaceAfterMonthPart(final String str);

    int indexOfMatchingText(String[] arr, String toMatch);

    Date getDate(String str);

    List<Date> getDateList(String str);

    List<String> getDateStrList(String str);

    String getLastDateStr(String str);

    boolean isNullOrEmpty(String s);

    String removeMultipleSpaces(String str);

    String removeSpacesAroundDashes(String str);

}
