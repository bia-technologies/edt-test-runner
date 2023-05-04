/*******************************************************************************
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package ru.biatech.edt.junit.launcher.lifecycle;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.debug.core.ILaunch;

import java.time.Instant;

@Getter
public class LifecycleItem {
  private final ILaunch testLaunch;
  @Setter
  private ILaunch mainLaunch;
  private final String name;
  private boolean active = true;
  private Instant start;
  private Instant end;

  public LifecycleItem(ILaunch testLaunch, String name) {
    this.testLaunch = testLaunch;
    this.name = name;
  }

  public void onStop() {
    active = true;
    end = Instant.now();
  }

  public void onStart() {
    active = true;
    start = Instant.now();
  }
}
