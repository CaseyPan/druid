/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.segment.nested;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.doubles.Double2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.apache.druid.segment.data.FrontCodedIntArrayIndexedWriter;

import javax.annotation.Nullable;

/**
 * Ingestion time dictionary identifier lookup, used by {@link NestedCommonFormatColumnSerializer} to build a global
 * dictionary id to value mapping for the 'stacked' global value dictionaries.
 */
public class DictionaryIdLookup
{
  private final Object2IntMap<String> stringLookup;

  private final Long2IntMap longLookup;

  private final Double2IntMap doubleLookup;

  private final Object2IntMap<int[]> arrayLookup;

  private int dictionarySize;

  public DictionaryIdLookup()
  {
    this.stringLookup = new Object2IntLinkedOpenHashMap<>();
    stringLookup.defaultReturnValue(-1);
    this.longLookup = new Long2IntLinkedOpenHashMap();
    longLookup.defaultReturnValue(-1);
    this.doubleLookup = new Double2IntLinkedOpenHashMap();
    doubleLookup.defaultReturnValue(-1);
    this.arrayLookup = new Object2IntAVLTreeMap<>(FrontCodedIntArrayIndexedWriter.ARRAY_COMPARATOR);
    this.arrayLookup.defaultReturnValue(-1);
  }

  public void addString(@Nullable String value)
  {
    Preconditions.checkState(
        longLookup.size() == 0 && doubleLookup.size() == 0,
        "All string values must be inserted to the lookup before long and double types"
    );
    int id = dictionarySize++;
    stringLookup.put(value, id);
  }

  // used when there are no string values to ensure that 0 is used for the null value
  public void addNumericNull()
  {
    Preconditions.checkState(
        stringLookup.size() == 0 && longLookup.size() == 0 && doubleLookup.size() == 0,
        "Lookup must be empty to add implicit null"
    );
    dictionarySize++;
  }

  public int lookupString(@Nullable String value)
  {
    return stringLookup.getInt(value);
  }

  public void addLong(long value)
  {
    Preconditions.checkState(
        doubleLookup.size() == 0,
        "All long values must be inserted to the lookup before double types"
    );
    int id = dictionarySize++;
    longLookup.put(value, id);
  }

  public int lookupLong(@Nullable Long value)
  {
    if (value == null) {
      return 0;
    }
    return longLookup.get(value.longValue());
  }

  public void addDouble(double value)
  {
    int id = dictionarySize++;
    doubleLookup.put(value, id);
  }

  public int lookupDouble(@Nullable Double value)
  {
    if (value == null) {
      return 0;
    }
    return doubleLookup.get(value.doubleValue());
  }

  public void addArray(int[] value)
  {
    int id = dictionarySize++;
    arrayLookup.put(value, id);
  }

  public int lookupArray(@Nullable int[] value)
  {
    if (value == null) {
      return 0;
    }
    return arrayLookup.getInt(value);
  }
}
