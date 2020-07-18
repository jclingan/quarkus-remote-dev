package org.acme;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativehelloIT extends helloTest {

    // Execute the same tests but in native mode.
}