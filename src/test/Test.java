package test;
/* Copyright (c) 2006 Olivier Elshocht
 *
 * Test.java
 *
 * Created on 24 novembre 2006, 2:08
 */

import classfile.*;

/**
 *
 * @author Olivier Elshocht
 */
public class Test
{
    public Test()
    {
    }

    public static void main(String[] args)
    {
        Test test = new Test();
        test.test1();
        test.test2();
        test.test3();
        test.test4();
    }

    private void test1()
    {
        System.out.println("Test1: clear text");
    }

    private void test2()
    {
        System.out.println("Test2: crypted text: " + Utf8.cryptString("SHOULD BE CRYPTED"));
    }

    private void test3()
    {
        String result = "OK";
        System.out.println("Test3: concatenating strings:" + result);
    }

    private void test4()
    {
        String functionName = "test4";
        System.out.println("Test4: some text equals to function name: " + functionName);
    }
}
