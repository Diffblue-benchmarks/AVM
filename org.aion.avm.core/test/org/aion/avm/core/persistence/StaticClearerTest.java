package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.Helper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class StaticClearerTest {
    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        new Helper(ReflectionStructureCodecTarget.class.getClassLoader(), 1_000_000L, 1);
    }

    @After
    public void tearDown() {
        Helper.clearTestingState();
    }

    @Test
    public void basicTest() {
        ReflectionStructureCodecTarget.s_one = true;
        ReflectionStructureCodecTarget.s_two = 5;
        ReflectionStructureCodecTarget.s_three = 5;
        ReflectionStructureCodecTarget.s_four = 5;
        ReflectionStructureCodecTarget.s_five = 5;
        ReflectionStructureCodecTarget.s_six = 5.0f;
        ReflectionStructureCodecTarget.s_seven = 5;
        ReflectionStructureCodecTarget.s_eight = 5.0d;
        ReflectionStructureCodecTarget.s_nine = new ReflectionStructureCodecTarget();
        
        List<Class<?>> classes = Collections.singletonList(ReflectionStructureCodecTarget.class);
        Map<Class<?>, Field[]> fields = Collections.singletonMap(ReflectionStructureCodecTarget.class, ReflectionStructureCodecTarget.class.getDeclaredFields());
        StaticClearer.nullAllStaticFields(classes, fields);
        
        // Prove that primitives are unchanged but the reference is null.
        Assert.assertEquals(true, ReflectionStructureCodecTarget.s_one);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_two);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_three);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_four);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_five);
        Assert.assertEquals(5.0f, ReflectionStructureCodecTarget.s_six, 0.1);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_seven);
        Assert.assertEquals(5.0d, ReflectionStructureCodecTarget.s_eight, 0.1);
        Assert.assertNull(ReflectionStructureCodecTarget.s_nine);
    }
}