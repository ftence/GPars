//  GPars (formerly GParallelizer)
//
//  Copyright © 2008-9  The original author or authors
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.


package groovyx.gpars

import java.util.concurrent.ConcurrentHashMap

/**
 * Author: Vaclav Pech
 * Date: Oct 30, 2009
 */
class ParallelTest extends GroovyTestCase {

    public void testParallelProperty() {
        shouldFail {
            [].makeTransparentlyParallel()
            [1].makeTransparentlyParallel()
            'abcde'.makeTransparentlyParallel()
        }

        def items = [1, 2, 3, 4, 5]

        shouldFail {
            items.makeTransparentlyParallel()
        }

        Parallelizer.doParallel {
            assertNotNull([1].makeTransparentlyParallel())
            assertNotNull('abcde'.makeTransparentlyParallel())
            assertTrue items == items.makeTransparentlyParallel()
            assertNotNull(items.makeTransparentlyParallel())
            assertTrue items.makeTransparentlyParallel() == items.makeTransparentlyParallel().makeTransparentlyParallel()
            final def p1 = items.makeTransparentlyParallel()
            assertTrue p1 == p1.makeTransparentlyParallel()
        }

        shouldFail {
            [1].makeTransparentlyParallel()
        }

        shouldFail {
            items.makeTransparentlyParallel()
        }
    }

    public void testParallelType() {
        Parallelizer.doParallel {
            Collection c = [1, 2, 3, 4, 5].makeTransparentlyParallel()
            String s = 'abcde'.makeTransparentlyParallel()
        }
    }

    public void testIsTransparentlyParallel() {
        def items = [1, 2, 3, 4, 5]
        shouldFail {
            items.transparentlyParallel
        }
        Parallelizer.doParallel {
            assertFalse items.isTransparentlyParallel()
            assertFalse 'abc'.isTransparentlyParallel()
            assertTrue items.makeTransparentlyParallel().isTransparentlyParallel()
            assertTrue items.isTransparentlyParallel()
            assertTrue 'abcde'.makeTransparentlyParallel().isTransparentlyParallel()
        }

        assertTrue items.isTransparentlyParallel()
        assertTrue 'abcde'.isTransparentlyParallel()
        shouldFail {
            assertTrue 'ab'.isTransparentlyParallel()
        }
    }

    public void testIsTransparentlyParallelWithTransparentParallelArrayUtil() {
        def items = [1, 2, 3, 4, 5]
        shouldFail {
            items.transparentlyParallel
        }
        Parallelizer.doTransparentlyParallel {
            assertTrue items.isTransparentlyParallel()
            assertTrue 'abc'.isTransparentlyParallel()
            shouldFail {
                items.makeTransparentlyParallel()
            }
            assertTrue items.isTransparentlyParallel()
            shouldFail {
                'abcde'.makeTransparentlyParallel()
            }
        }

        shouldFail {
            assertFalse items.isTransparentlyParallel()
        }
        shouldFail {
            assertFalse 'abcde'.isTransparentlyParallel()
        }
        shouldFail {
            assertTrue 'ab'.isTransparentlyParallel()
        }
    }

    public void testNestedParallel() {
        def items = [1, 2, 3, 4, 5]
        final ConcurrentHashMap map = new ConcurrentHashMap()
        Parallelizer.doParallel(5) {
            items.makeTransparentlyParallel().makeTransparentlyParallel().each {
                Thread.sleep 500
                map[Thread.currentThread()] = ''
            }
        }
        assert map.keys().size() > 1
    }

    public void testChainedParallel() {
        def items = [1, 2, 3, 4, 5]
        final ConcurrentHashMap map = new ConcurrentHashMap()
        Parallelizer.doParallel(5) {
            items.makeTransparentlyParallel().collect {it * 2}.findAll {it > 1}.each {
                Thread.sleep 500
                map[Thread.currentThread()] = ''
            }
        }
        assert map.keys().size() > 3
    }

    public void testParallelWithString() {
        def items = 'abcde'
        final ConcurrentHashMap map = new ConcurrentHashMap()
        Parallelizer.doParallel(5) {
            items.makeTransparentlyParallel().collect {it * 2}.findAll {it.size() > 1}.each {
                Thread.sleep 500
                map[Thread.currentThread()] = ''
            }
        }
        assert map.keys().size() > 3
    }

    public void testParallelWithIterator() {
        def items = [1, 2, 3, 4, 5].iterator()
        final ConcurrentHashMap map = new ConcurrentHashMap()
        Parallelizer.doParallel(5) {
            items.makeTransparentlyParallel().collect {it * 2}.findAll {it > 1}.each {
                Thread.sleep 500
                map[Thread.currentThread()] = ''
            }
        }
        assert map.keys().size() > 3
    }

    public void testParallelInMethodCall() {
        def items = [1, 2, 3, 4, 5]
        assertEquals 1, foo(items).keys().size()

        Parallelizer.doParallel(5) {
            assertEquals 1, foo(items).keys().size()
            assert foo(items.makeTransparentlyParallel()).keys().size() > 3
        }
    }

    private def foo(Collection c) {
        final ConcurrentHashMap map = new ConcurrentHashMap()
        c.collect {it * 2}.findAll {it > 1}.each {
            Thread.sleep 50
            map[Thread.currentThread()] = ''
        }
        return map
    }

    public void testTransparentEach() {
        def items = [1, 2, 3, 4, 5]
        final ConcurrentHashMap map = new ConcurrentHashMap()
        Parallelizer.doParallel(5) {
            items.makeTransparentlyParallel().each {
                Thread.sleep 100
                map[Thread.currentThread()] = ''
            }
        }
        assert map.keys().size() > 1
    }

    public void testTransparentEachWithIndex() {
        def items = [1, 2, 3, 4, 5]
        final ConcurrentHashMap map = new ConcurrentHashMap()
        Parallelizer.doParallel(5) {
            items.makeTransparentlyParallel().eachWithIndex {e, i ->
                Thread.sleep 100
                map[Thread.currentThread()] = ''
            }
        }
        assert map.keys().size() > 1
    }

    //todo test mixing transparent and non-transparent enhancements in all variants

    public void testUsingNonTransparentEachInTransparentContext() {
        def items = [1, 2, 3, 4, 5]
        final ConcurrentHashMap map = new ConcurrentHashMap()
        Parallelizer.doParallel(5) {
            items.makeTransparentlyParallel().eachParallel {
                Thread.sleep 100
                map[Thread.currentThread()] = ''
            }
        }
        assert map.keys().size() > 1
    }

    public void testUsingTransparentEachInTransparentContext() {
        def items = [1, 2, 3, 4, 5]
        final ConcurrentHashMap map = new ConcurrentHashMap()
        Parallelizer.doTransparentlyParallel(5) {
            Parallelizer.doParallel(5) {
                items.makeTransparentlyParallel().each {
                    Thread.sleep 100
                    map[Thread.currentThread()] = ''
                }
            }
        }
        assert map.keys().size() > 1
    }

}