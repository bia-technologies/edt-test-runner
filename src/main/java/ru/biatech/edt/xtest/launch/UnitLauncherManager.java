/*
 * Copyright 2021-2022 BIA-Technologies Limited Liability Company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * За основу взят класс https://github.com/DoublesunRUS/ru.capralow.dt.unit/blob/master/bundles/ru.capralow.dt.unit.launcher.ui/src/ru/capralow/dt/unit/launcher/internal/ui/UnitLauncherManager.java
 * Copyright (c) 2020, Alexander Kapralov
 */

package ru.biatech.edt.xtest.launch;

import org.eclipse.debug.core.DebugPlugin;

import com._1c.g5.wiring.IManagedService;


public class UnitLauncherManager implements IManagedService {
    private ShowJUnitResult showJUnitResult;
//    private TestCaseListener testCaseListener;

    @Override
    public void activate() {
        showJUnitResult = new ShowJUnitResult();
        DebugPlugin.getDefault().addDebugEventListener(showJUnitResult);
    }

    @Override
    public void deactivate() {
        DebugPlugin.getDefault().removeDebugEventListener(showJUnitResult);
    }
}
