/*******************************************************************************
 * Copyright (c) 2024 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.yaxunit.mocks;

import com._1c.g5.v8.dt.bsl.model.Function;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.Pragma;
import com._1c.g5.v8.dt.bsl.services.BslGrammarAccess;
import com._1c.g5.v8.dt.bsl.ui.BslGeneratorMultiLangProposals;
import com._1c.g5.v8.dt.bsl.ui.contentassist.BslProposalProvider;
import com._1c.g5.v8.dt.mcore.NamedElement;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MethodMockDefinition implements MockDefinition {
  private final Method method;
  protected String name;
  protected List<String> params;
  boolean export;
  boolean function;
  private boolean isRussian;

  public MethodMockDefinition(Method method) {
    this.method = method;
    name = method.getName();
    export = method.isExport();
    function = method instanceof Function;
    params = method.getFormalParams().stream()
        .map(NamedElement::getName)
        .collect(Collectors.toList());
  }

  public MethodMockDefinition(Module module, String methodName) {
    this(getMethod(module, methodName));
  }

  private static Method getMethod(Module module, String methodName) {
    return module.allMethods().stream()
        .filter(m -> m.getName().equals(methodName))
        .findFirst()
        .orElseThrow();
  }

  @Override
  public void setIsRussian(boolean value) {
    isRussian = value;
  }

  @Override
  public List<Pragma> getPragmas() {
    return method.getPragmas();
  }

  @Override
  public String getParamsDefinition(BslGeneratorMultiLangProposals bslGenProp, BslGrammarAccess bslGrammar) {
    return method.getFormalParams().stream()
        .map(p -> p.isByValue() ? BslProposalProvider.getByValueLiteralName(bslGrammar, isRussian) + bslGenProp.getSpacePropStr() + p.getName() : p.getName())
        .collect(Collectors.joining(bslGenProp.getCommaPropStr()));
  }
}
