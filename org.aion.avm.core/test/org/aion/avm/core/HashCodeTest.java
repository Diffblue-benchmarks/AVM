package org.aion.avm.core;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.internal.Helper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests the hashCode behaviour of the contract code.  Includes a tests that our helpers/instrumentation don't invalidate Java assumptions.
 */
public class HashCodeTest {
    @Before
    public void setup() throws Exception {
        SimpleRuntime rt = new SimpleRuntime(null, null, 10000);
        Helper.setBlockchainRuntime(rt);
    }

    @After
    public void teardown() throws Exception {
        Helper.clearTestingState();
    }

    /**
     * Tests that we can invoke the entire transformation pipeline for a basic test.
     * This test just verifies that we can do the transformation, not that the transformed class is correct or can be run.
     * Arguably, this test belongs in AvmImplTest, but it acts on a common test target and environmental shape as the other tests here
     * so we will build it here in the hopes that it informs the same evolution of common testing infrastructure.
     */
    @Test
    public void testBasicTranslation() throws Exception {
        Class<?> clazz = commonLoadTestClass();
        Assert.assertNotNull(clazz);
    }

    @Test
    public void testCommonHash() throws Exception {
        Class<?> clazz = commonLoadTestClass();
        Assert.assertNotNull(clazz);
        
        Object result = clazz.getMethod("getOneHashCode").invoke(null);
        Assert.assertEquals(1, ((Integer)result).intValue());
        result = clazz.getMethod("getOneHashCode").invoke(null);
        Assert.assertEquals(2, ((Integer)result).intValue());
        clazz.getConstructor().newInstance();
        result = clazz.getMethod("getOneHashCode").invoke(null);
        Assert.assertEquals(4, ((Integer)result).intValue());
    }


    private Class<?> commonLoadTestClass() throws ClassNotFoundException {
        ClassLoader parentLoader = HashCodeTest.class.getClassLoader();
        String className = HashCodeTestTarget.class.getName();
        byte[] raw = TestClassLoader.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", raw)
                .asMutableForest();
        
        AvmImpl avm = new AvmImpl();
        Map<String, Integer> runtimeObjectSizes = avm.computeRuntimeObjectSizes();
        Map<String, Integer> allObjectSizes = avm.computeObjectSizes(classHierarchy, runtimeObjectSizes);
        Function<byte[], byte[]> transformer = (inputBytes) -> {
            return avm.transformClasses(Collections.singletonMap(className, inputBytes), classHierarchy, allObjectSizes).get(className);
        };
        TestClassLoader loader = new TestClassLoader(parentLoader, transformer);
        loader.addClassForRewrite(className, raw);
        Class<?> clazz = loader.loadClass(className);
        Assert.assertEquals(loader, clazz.getClassLoader());
        return clazz;
    }
}
