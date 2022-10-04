/*******************************************************************************
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.v8utils;

import com._1c.g5.v8.dt.bm.index.emf.IBmEmfIndexProvider;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;

import java.util.Iterator;

public final class MdUtils {
  /**
   * Copyright (c) 2020, Alexander Kapralov
   */
  public static MdObject getConfigurationObject(EClass objectClass, String objectName,
                                                IBmEmfIndexProvider bmEmfIndexProvider) {
    QualifiedName qnObjectName = getConfigurationObjectQualifiedName(objectName, objectClass);

    MdObject object = null;

    Iterable<IEObjectDescription> objectIndex = bmEmfIndexProvider.getEObjectIndexByType(objectClass, qnObjectName, true);
    Iterator<IEObjectDescription> objectItr = objectIndex.iterator();
    if (objectItr.hasNext())
      object = (MdObject) objectItr.next().getEObjectOrProxy();

    return object;
  }

  public static IResource getResource(EObject object) {
    return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(EcoreUtil.getURI(object).toPlatformString(true)));
  }

  private static QualifiedName getConfigurationObjectQualifiedName(String objectName, EClass mdLiteral) {
    String[] objectArray = objectName.split("\\."); //$NON-NLS-1$

    QualifiedName qnObjectName = null;
    for (String objectValue : objectArray) {
      if (qnObjectName == null) {
        qnObjectName = QualifiedName.create(mdLiteral.getName(), objectValue);
      } else {
        if (mdLiteral.equals(MdClassPackage.Literals.SUBSYSTEM))
          qnObjectName = qnObjectName.append(QualifiedName.create(mdLiteral.getName(), objectValue));

        else
          qnObjectName = qnObjectName.append(QualifiedName.create(objectValue));
      }
    }

    return qnObjectName;
  }
}