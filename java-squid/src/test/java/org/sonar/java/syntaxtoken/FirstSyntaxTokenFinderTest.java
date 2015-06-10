/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.syntaxtoken;

import com.google.common.base.Charsets;
import org.junit.Test;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.SwitchStatementTree;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TryStatementTree;
import org.sonar.plugins.java.api.tree.VariableTree;

import static org.fest.assertions.Assertions.assertThat;

public class FirstSyntaxTokenFinderTest {

  @Test
  public void compilationUnit() {
    CompilationUnitTree compilationUnit = getCompilationUnit("class Test {}");
    SyntaxToken firstToken = getFirstSyntaxToken(compilationUnit);
    assertThat(firstToken.text()).isEqualTo("class");

    compilationUnit = getCompilationUnit("import A; class Test {}");
    firstToken = getFirstSyntaxToken(compilationUnit);
    assertThat(firstToken.text()).isEqualTo("import");

    compilationUnit = getCompilationUnit("package myPackage; import A; class Test {}");
    firstToken = getFirstSyntaxToken(compilationUnit);
    assertThat(firstToken.text()).isEqualTo("myPackage");

    compilationUnit = getCompilationUnit("");
    firstToken = getFirstSyntaxToken(compilationUnit);
    assertThat(firstToken).isNull();
  }

  @Test
  public void classes() {
    CompilationUnitTree compilationUnit = getCompilationUnit("class Test {}");
    SyntaxToken firstToken = getFirstSyntaxToken(getFirstClass(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("class");

    compilationUnit = getCompilationUnit("public class Test {}");
    firstToken = getFirstSyntaxToken(getFirstClass(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("public");

    compilationUnit = getCompilationUnit("public abstract class Test {}");
    firstToken = getFirstSyntaxToken(getFirstClass(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("public");

    compilationUnit = getCompilationUnit("public abstract class Test {}");
    firstToken = getFirstSyntaxToken(getFirstClass(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("public");

    compilationUnit = getCompilationUnit("@Deprecated public abstract class Test {}");
    ClassTree firstClass = getFirstClass(compilationUnit);
    firstToken = getFirstSyntaxToken(firstClass.modifiers().get(0));
    assertThat(firstToken.text()).isEqualTo("@");

    compilationUnit = getCompilationUnit("class A { A a = new A(){}; }");
    NewClassTree newClassTree = (NewClassTree) getFirstVariable(getFirstClass(compilationUnit)).initializer();
    firstToken = getFirstSyntaxToken(newClassTree.classBody());
    assertThat(firstToken.text()).isEqualTo("{");
  }

  @Test
  public void variable() {
    CompilationUnitTree compilationUnit = getCompilationUnit("class Test { Integer i; }");
    SyntaxToken firstToken = getFirstSyntaxToken(getFirstVariable(getFirstClass(compilationUnit)));
    assertThat(firstToken.text()).isEqualTo("Integer");

    compilationUnit = getCompilationUnit("class Test { Integer[] i; }");
    firstToken = getFirstSyntaxToken(getFirstVariable(getFirstClass(compilationUnit)));
    assertThat(firstToken.text()).isEqualTo("Integer");

    compilationUnit = getCompilationUnit("class Test { private Integer i; }");
    firstToken = getFirstSyntaxToken(getFirstVariable(getFirstClass(compilationUnit)));
    assertThat(firstToken.text()).isEqualTo("private");

    compilationUnit = getCompilationUnit("class Test { Java.lang.List l; }");
    firstToken = getFirstSyntaxToken(getFirstVariable(getFirstClass(compilationUnit)));
    assertThat(firstToken.text()).isEqualTo("Java");
  }

  @Test
  public void method() {
    CompilationUnitTree compilationUnit = getCompilationUnit("class Test { void foo() {} }");
    SyntaxToken firstToken = getFirstSyntaxToken(getFirstMethod(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("void");

    compilationUnit = getCompilationUnit("class Test { private void foo() {} }");
    firstToken = getFirstSyntaxToken(getFirstMethod(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("private");

    compilationUnit = getCompilationUnit("class Test { Test() {} }");
    firstToken = getFirstSyntaxToken(getFirstMethod(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("Test");

    compilationUnit = getCompilationUnit("class Test { <T> void Test(T t) {} }");
    firstToken = getFirstSyntaxToken(getFirstMethod(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("<");

    compilationUnit = getCompilationUnit("class Test<T> { T Test() {} }");
    firstToken = getFirstSyntaxToken(getFirstMethod(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("T");

    compilationUnit = getCompilationUnit("class Test<T> { T Test() {} }");
    firstToken = getFirstSyntaxToken(getFirstMethod(compilationUnit).block());
    assertThat(firstToken.text()).isEqualTo("{");
  }

  @Test
  public void enumeration() {
    CompilationUnitTree compilationUnit = getCompilationUnit("enum Test { }");
    SyntaxToken firstToken = getFirstSyntaxToken(getFirstClass(compilationUnit));
    assertThat(firstToken.text()).isEqualTo("enum");

    compilationUnit = getCompilationUnit("enum Test { A; }");
    firstToken = getFirstSyntaxToken(getFirstClass(compilationUnit).members().get(0));
    assertThat(firstToken.text()).isEqualTo("A");
  }

  @Test
  public void modifiers() {
    String p = "class Foo {}";
    ClassTree c = getFirstClass(getCompilationUnit(p));
    SyntaxToken firstToken = getFirstSyntaxToken(c.modifiers());
    assertThat(firstToken).isNull();

    p = "public @Deprecated class Foo {}";
    c = getFirstClass(getCompilationUnit(p));
    firstToken = getFirstSyntaxToken(c.modifiers());
    assertThat(firstToken.text()).isEqualTo("public");

    p = "@Deprecated public class Foo {}";
    c = getFirstClass(getCompilationUnit(p));
    firstToken = getFirstSyntaxToken(c.modifiers());
    assertThat(firstToken.text()).isEqualTo("@");
  }

  @Test
  public void wildcard() {
    String p =
      "class Foo {"
        + "  void foo(Collection<?> c) {"
        + "  }"
        + "}";
    MethodTree m = getFirstMethod(getCompilationUnit(p));
    SyntaxToken firstToken = getFirstSyntaxToken(((ParameterizedTypeTree) m.parameters().get(0).type()).typeArguments().get(0));
    assertThat(firstToken.text()).isEqualTo("?");
  }

  @Test
  public void type_parameters() {
    String p = "class Foo<T> {}";
    ClassTree c = getFirstClass(getCompilationUnit(p));
    SyntaxToken firstToken = getFirstSyntaxToken(c.typeParameters());
    assertThat(firstToken.text()).isEqualTo("<");

    firstToken = getFirstSyntaxToken(c.typeParameters().get(0));
    assertThat(firstToken.text()).isEqualTo("T");
  }

  @Test
  public void type_arguments() {
    String p = "class Foo {"
      + "  void foo(Collection<Foo> c) {"
      + "  }"
      + "}";
    MethodTree m = getFirstMethod(getCompilationUnit(p));
    SyntaxToken firstToken = getFirstSyntaxToken(((ParameterizedTypeTree) m.parameters().get(0).type()).typeArguments());
    assertThat(firstToken.text()).isEqualTo("<");
  }

  @Test
  public void static_initializer() {
    String p =
      "class Foo {"
        + "  static {"
        + "  }"
        + "}";
    ClassTree classTree = getFirstClass(getCompilationUnit(p));
    SyntaxToken firstToken = getFirstSyntaxToken(classTree.members().get(0));
    assertThat(firstToken.text()).isEqualTo("static");
  }

  @Test
  public void try_catch() {
    CompilationUnitTree compilationUnit = getCompilationUnit(
      "class Foo {"
        + "  void foo() {"
        + "    try { }"
        + "    catch(Exception e) { }"
        + "  }"
        + "}");

    TryStatementTree tryStatementTree = (TryStatementTree) getFirstMethod(compilationUnit).block().body().get(0);
    SyntaxToken firstToken = getFirstSyntaxToken(tryStatementTree);
    assertThat(firstToken.text()).isEqualTo("try");

    firstToken = getFirstSyntaxToken(tryStatementTree.catches().get(0));
    assertThat(firstToken.text()).isEqualTo("catch");
  }

  @Test
  public void union_type() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    try {"
        + "    } catch (IOException | SQLException ex) {"
        + "    }"
        + "  }"
        + "}";
    TryStatementTree t = (TryStatementTree) getFirstStatement(p);
    SyntaxToken firstToken = getFirstSyntaxToken(t.catches().get(0).parameter());
    assertThat(firstToken.text()).isEqualTo("IOException");
  }

  @Test
  public void empty_statement() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    ;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, ";");
  }

  @Test
  public void expression() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    bar();"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "bar");
  }

  @Test
  public void if_statement() {
    String p =
      "class Foo {"
        + "  void foo(boolean test) {"
        + "    if (test) { }"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "if");
  }

  @Test
  public void assert_statement() {
    String p =
      "class Foo {"
        + "  void foo(boolean test) {"
        + "    assert true;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "assert");
  }

  @Test
  public void switch_statement() {
    String p =
      "class Foo {"
        + "  void foo(MyEnum myEnum) {"
        + "    switch(myEnum) {"
        + "      case A:"
        + "    }"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "switch");

    SwitchStatementTree switchStatementTree = (SwitchStatementTree) getFirstStatement(p);
    SyntaxToken firstToken = getFirstSyntaxToken(switchStatementTree.cases().get(0));
    assertThat(firstToken.text()).isEqualTo("case");
  }

  @Test
  public void while_statement() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    while(true) {"
        + "    }"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "while");
  }

  @Test
  public void do_while_statement() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    do {"
        + "    } while (true);"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "do");
  }

  @Test
  public void for_statement() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    for( ; ; ) {"
        + "    }"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "for");
  }

  @Test
  public void foreach_statement() {
    String p =
      "class Foo {"
        + "  void foo(List list) {"
        + "    for(Object o : list) {"
        + "    }"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "for");
  }

  @Test
  public void break_statement() {
    String p =
      "class Foo {"
        + "  void foo(List list) {"
        + "    break;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "break");
  }

  @Test
  public void continue_statement() {
    String p =
      "class Foo {"
        + "  void foo(List list) {"
        + "    continue;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "continue");
  }

  @Test
  public void return_statement() {
    String p =
      "class Foo {"
        + "  void foo(List list) {"
        + "    return;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "return");
  }

  @Test
  public void throw_statement() {
    String p =
      "class Foo {"
        + "  void foo(List list) {"
        + "    throw new Exception();"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "throw");
  }

  @Test
  public void synchronized_statement() {
    String p =
      "class Foo {"
        + "  void foo(List list) {"
        + "    synchronized (new Object()) {}"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "synchronized");
  }

  @Test
  public void new_class_statement() {
    String p =
      "class Foo {"
        + "  void foo(List list) {"
        + "    new Foo().toString();"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "new");

    p =
      "class T {"
        + "  T m() {"
        + "    this.new T(true, false) {};"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "this");
  }

  @Test
  public void literal() {
    String p =
      "class Foo {"
        + "  void foo(List list) {"
        + "    \"test\".length();"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "\"test\"");
  }

  @Test
  public void parameterizedType() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    Set<Class<?>> set = newSet();"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "Set");
  }

  @Test
  public void label() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    FOO:"
        + "      return;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "FOO");
  }

  @Test
  public void unary_expression() {
    String p =
      "class Foo {"
        + "  void foo(int i) {"
        + "    ++i;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "++");

    p =
      "class Foo {"
        + "  void foo(int i) {"
        + "    i++;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "i");
  }

  @Test
  public void parenthesis() {
    String p =
      "class Foo {"
        + "  void foo(List list) {"
        + "    ((Object) list).toString();"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "(");
  }

  @Test
  public void binaryExpression() {
    String p =
      "class Foo {"
        + "  void foo(int a, int b) {"
        + "    a + b;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "a");
  }

  @Test
  public void array_access() {
    String p =
      "class Foo {"
        + "  void foo(int[] array) {"
        + "    array[4] = 2;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "array");
  }

  @Test
  public void conditional_expression() {
    String p =
      "class Foo {"
        + "  void foo(boolean test) {"
        + "    test ? 1 : 2;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "test");
  }

  @Test
  public void new_array() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    new int[3];"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "new");
  }

  @Test
  public void type_cast() {
    String p =
      "class Foo {"
        + "  void foo(Foo f) {"
        + "    (Object) f;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "(");
  }

  @Test
  public void instance_of() {
    String p =
      "class Foo {"
        + "  void foo(Foo f) {"
        + "    f instanceof Object;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "f");
  }

  @Test
  public void lambda() {
    String p =
      "class Foo {"
        + "  void foo(Person p) {"
        + "    p -> p.printPerson();"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "p");

    p =
      "class Foo {"
        + "  void foo(Person p1, Person p2) {"
        + "    (p1, p2) -> p1.isTallerThan(p2);"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "(");
  }

  @Test
  public void method_reference() {
    String p =
      "class Foo {"
        + "  void foo() {"
        + "    HashSet::new;"
        + "  }"
        + "}";
    assertFirstStatementFirstTokenValue(p, "HashSet");
  }

  private void assertFirstStatementFirstTokenValue(String p, String expected) {
    assertThat(getFirstSyntaxToken(getFirstStatement(p)).text()).isEqualTo(expected);
  }

  private SyntaxToken getFirstSyntaxToken(Tree tree) {
    return FirstSyntaxTokenFinder.firstSyntaxToken(tree);
  }

  private CompilationUnitTree getCompilationUnit(String p) {
    return (CompilationUnitTree) JavaParser.createParser(Charsets.UTF_8).parse(p);
  }

  private MethodTree getFirstMethod(CompilationUnitTree compilationUnitTree) {
    return getFirstMethod(getFirstClass(compilationUnitTree));
  }

  private ClassTree getFirstClass(CompilationUnitTree compilationUnitTree) {
    return ((ClassTree) compilationUnitTree.types().get(0));
  }

  private MethodTree getFirstMethod(ClassTree classTree) {
    return (MethodTree) classTree.members().get(0);
  }

  private VariableTree getFirstVariable(ClassTree classTree) {
    return (VariableTree) classTree.members().get(0);
  }

  private StatementTree getFirstStatement(String p) {
    CompilationUnitTree compilationUnit = getCompilationUnit(p);
    return getFirstMethod(getFirstClass(compilationUnit)).block().body().get(0);
  }
}