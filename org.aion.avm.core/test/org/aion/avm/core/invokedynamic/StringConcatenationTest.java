package org.aion.avm.core.invokedynamic;

import org.aion.avm.core.*;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.core.types.Forest;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.PackageConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.aion.avm.core.invokedynamic.InvokedynamicUtils.buildSingletonAccessRules;
import static org.aion.avm.core.util.Helpers.loadRequiredResourceAsBytes;
import static org.junit.Assert.assertFalse;

public class StringConcatenationTest {
    private static String HELPER_CLASS_NAME = PackageConstants.kInternalSlashPrefix + "Helper";

    @Before
    public void init() {
        final var avmClassLoader = NodeEnvironment.singleton.createInvocationClassLoader(Collections.emptyMap());
        new Helper(avmClassLoader, 1_000_000L, 1);
    }

    @After
    public void teardown() {
        Helper.clearTestingState();
    }

    @Test
    public void given_stringConcatLambda_then_bootstrapMethodShouldBeShadowed() throws Exception {
        final var clazz = transformClass(IndyConcatenationTestResource.class);
        final var actual = callMethod(clazz, "avm_concatWithDynamicArgs",
                new org.aion.avm.shadow.java.lang.String("a"),
                new org.aion.avm.shadow.java.lang.String("b"),
                new org.aion.avm.shadow.java.lang.String("c"));
        Assert.assertEquals("abc", actual);
    }

    @Test
    public void given_stringConcatLambda_then_bootstrapMethodShouldBeShadowed2() throws Exception {
        final var clazz = transformClass(IndyConcatenationTestResource.class);
        final var actual = callMethod(clazz, "avm_concatWithCharacters",
                new org.aion.avm.shadow.java.lang.String("a"),
                new org.aion.avm.shadow.java.lang.String("b"),
                new org.aion.avm.shadow.java.lang.String("c"));
        Assert.assertEquals("yabcx12.8", actual);
    }

    private static String callMethod(Class<?> clazz,
                                     String methodName, org.aion.avm.shadow.java.lang.String... stringArgs) throws Exception {
        final var instance = clazz.getDeclaredConstructor().newInstance();
        final var method = clazz.getDeclaredMethod(methodName, getTypesFrom(stringArgs));
        final var actual = (org.aion.avm.shadow.java.lang.String) method.invoke(instance, stringArgs);
        return actual.getUnderlying();
    }

    private static Class<?>[] getTypesFrom(Object[] objects) {
        return Stream.of(objects).map(Object::getClass).toArray(Class[]::new);
    }

    private static Class<?> transformClass(Class<?> clazz) throws ClassNotFoundException {
        final var className = clazz.getName();
        final byte[] origBytecode = loadRequiredResourceAsBytes(InvokedynamicUtils.getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForStringConcatTest(origBytecode, className);
        assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        java.lang.String mappedClassName = PackageConstants.kUserDotPrefix + className;
        final var loader = new AvmSharedClassLoader(Map.of(mappedClassName, transformedBytecode));
        return loader.loadClass(mappedClassName);
    }

    private static byte[] transformForStringConcatTest(byte[] origBytecode, String className) {
        final Forest<String, ClassInfo> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", false, origBytecode)
                .asMutableForest();
        final var shadowPackage = PackageConstants.kShadowSlashPrefix;
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new UserClassMappingVisitor(new NamespaceMapper(buildSingletonAccessRules(classHierarchy, className))))
                .addNextVisitor(new ConstantVisitor(HELPER_CLASS_NAME))
                .addNextVisitor(new ClassShadowing(HELPER_CLASS_NAME))
                .addNextVisitor(new InvokedynamicShadower(shadowPackage))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        new ParentPointers(Collections.singleton(className), classHierarchy),
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }
}